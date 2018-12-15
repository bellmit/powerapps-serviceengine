package com.profitera.services.system.loan.impl;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.services.system.loan.IAccountTypes;
import com.profitera.util.DateParser;
import com.profitera.util.MapListUtil;

public class AgeCalculator {
  private static final String PRINCIPAL_TYPE = "PRINCIPAL";
  private static final String INTEREST_TYPE = "INTEREST";
  private static final String DATE = "DATE";
  private static final String TYPE = "TYPE";
  private static final String AMOUNT = "AMOUNT";
  public static final String ACCOUNT_TYPE_CODE = "ACCOUNT_TYPE_CODE";
  
  private class Installment implements Comparable {
    private final Date date;
    private final BigDecimal amount;

    private Installment(Date d, BigDecimal amount) {
      date = d;
      this.amount = amount;
    }

    public int compareTo(Object o) {
      Installment other = (Installment) o;
      return other.date.compareTo(date);
    }
  }
  private static Log LOG;
  private BigDecimal pInst;
  private BigDecimal interest;
  private BigDecimal iis;
  private final IInstallmentProvider provider;
  private List interestInstallments = new ArrayList();
  private List principalInstallments = new ArrayList();
  private final Long id;
  private boolean isExhausted = false;

  private Log getLog(){
    if (LOG == null) {
      LOG = LogFactory.getLog(getClass());
    }
    return LOG;
  }

  public AgeCalculator(Long id, List balances, IInstallmentProvider provider){
    this.id = id;
    if (provider == null) {
      throw new IllegalArgumentException("Installment information provider required");
    }
    this.provider = provider;
    if (id == null) {
      throw new IllegalArgumentException("No identifier assigned for age calcuation");
    }
    pInst = getBalance(id, IAccountTypes.PINST, balances);
    interest = getBalance(id, IAccountTypes.INTEREST, balances);
    iis = getBalance(id, IAccountTypes.IIS, balances);
  }

  private BigDecimal getBalance(Long id, String code, List balances) {
    BigDecimal amount;
    int index = MapListUtil.firstIndexOf(ACCOUNT_TYPE_CODE, code, balances);
    if (index == -1) {
      throw new IllegalArgumentException("Missing account type " + code + " in balances for account age calcuation for " + id);
    }
    amount = (BigDecimal) ((Map)balances.get(index)).get(AMOUNT);
    return amount;
  }

  public synchronized Date getOldestOverdueDate() throws SQLException {
    Date interestDate = allocateInstallments(interestInstallments, interest, "Interest");
    Date pDate = allocateInstallments(principalInstallments, pInst, "Inst. Principal");
    Date age = DateParser.getEarlierDate(interestDate, pDate);
    getLog().info("Age for account " + id + " determined to be " + age + " on interest balance of " + interest + " and principal installment balance of " + pInst);
    return age;
  }
  
  public synchronized Date getSuspenseOldestOverdueDate() throws SQLException {
    BigDecimal totalInterest = interest.add(iis);
    Date interestDate = allocateInstallments(interestInstallments, totalInterest, "Suspense + Interest");
    Date pDate = allocateInstallments(principalInstallments, pInst, "Inst. Principal");
    Date age = DateParser.getEarlierDate(interestDate, pDate);
    getLog().info("Age with suspense balance for account " + id + " determined to be " + age + " on interest balance of " + totalInterest + " and principal installment balance of " + pInst);
    return age;
  }

  private Date allocateInstallments(List installments,
      BigDecimal outstanding, String outstandingType) throws SQLException {
    getLog().debug("Allocating installments for " + id + " on " + outstanding + " for " + outstandingType);
    if (outstanding.compareTo(BigDecimal.ZERO) <= 0){
      return null;
    }
    while(true) {
      BigDecimal curr = outstanding;
      for (Iterator i = installments.iterator(); i.hasNext();) {
        Installment inst = (Installment) i.next();
        getLog().debug("Allocating installment for " + id + " on " + curr + " of " + inst.amount + " for " + outstandingType);
        if (inst.amount.compareTo(curr) >= 0){
          getLog().debug("Last allocated installment for " + id + " of " + inst.amount + " on " + inst.date + " for " + outstandingType);
          return inst.date;
        } else {
          curr = curr.subtract(inst.amount);
        }
      }
      int currentCount = installments.size();
      Date lastDate = getLastDate(installments);
      fetchInstallments();
      if (installments.size() == 0) {
        getLog().debug("Allocated all installments for " + id + " for " + outstandingType);
        return null;
      } else if (installments.size() == currentCount) {
        getLog().debug("Allocated all installments for " + id + " for " + outstandingType);
        return lastDate;
      }
    }
  }
  
  private Date getLastDate(List installments){
    if (installments.size() == 0){
      return null;
    }
    Installment i = (Installment) installments.get(installments.size() - 1);
    return i.date;
  }

  private void fetchInstallments() throws SQLException {
    if (isExhausted) return;
    Date p = getLastDate(principalInstallments);
    Date interest = getLastDate(interestInstallments);
    Date laterDate = DateParser.getEarlierDate(p, interest);
    List newInstallments = provider.fetchNextInstallments(laterDate);
    if (newInstallments.size() == 0) {
      isExhausted = true;
      return;
    }
    for (Iterator i = newInstallments.iterator(); i.hasNext();) {
      Map data = (Map) i.next();
      String type = (String) data.get(TYPE);
      BigDecimal amount = (BigDecimal) data.get(AMOUNT);
      Date date = (Date) data.get(DATE);
      if (amount == null) {
        throw new IllegalArgumentException("No AMOUNT for installment fetched for " + getId());
      } else if (date == null){
        throw new IllegalArgumentException("No DATE for installment fetched for " + getId());
      } else if (type == null) {
        throw new IllegalArgumentException("No TYPE for installment fetched for " + getId());
      }
      Installment installment = new Installment(date, amount);
      if (type.equals(INTEREST_TYPE)){
        interestInstallments.add(installment);
      } else if (type.equals(PRINCIPAL_TYPE)){
        principalInstallments.add(installment);
      } else {
        throw new IllegalArgumentException("Installment fetched for " + getId() + " has invalid type: " + type);
      }
    }
    Collections.sort(interestInstallments);
    Collections.sort(principalInstallments);
  }

  private Long getId() {
    return id;
  }
}
