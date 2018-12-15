package com.profitera.persistence.impl;

import oracle.toplink.mappings.DirectToFieldMapping;
import oracle.toplink.mappings.OneToOneMapping;
import oracle.toplink.publicinterface.Descriptor;

public class PaymentMapping {
  public Descriptor buildPayment() {
    Descriptor descriptor = new Descriptor();
    descriptor.setJavaClass(com.profitera.descriptor.db.payment.Payment.class);
    descriptor.addTableName("PTRPAYMENT");
    descriptor.addPrimaryKeyFieldName("PTRPAYMENT.PAYMENT_ID");

    // Inheritance properties.
    descriptor.getInheritancePolicy().setClassIndicatorFieldName(
        "PTRPAYMENT.PAYMENT_TYPE_ID");
    descriptor.getInheritancePolicy().addClassIndicator(
        com.profitera.descriptor.db.payment.PaymentDebitAdjustment.class,
        new java.lang.Long(9));
    descriptor.getInheritancePolicy().addClassIndicator(
        com.profitera.descriptor.db.payment.PaymentCash.class,
        new java.lang.Long(4));
    descriptor.getInheritancePolicy().addClassIndicator(
        com.profitera.descriptor.db.payment.PaymentCheck.class,
        new java.lang.Long(1));

    // Descriptor properties.
    descriptor.useSoftCacheWeakIdentityMap();
    descriptor.setIdentityMapSize(100);
    descriptor.useRemoteSoftCacheWeakIdentityMap();
    descriptor.setRemoteIdentityMapSize(100);
    descriptor.setSequenceNumberFieldName("PTRPAYMENT.PAYMENT_ID");
    descriptor.setSequenceNumberName("PAYMENT_ID_SEQ");
    descriptor.setAlias("Ptrpayment");

    // Query manager.
    descriptor.getQueryManager().checkCacheForDoesExist();

    DirectToFieldMapping fromHostMapping = new DirectToFieldMapping();
    fromHostMapping.setAttributeName("fromHost");
    fromHostMapping.setFieldName("PTRPAYMENT.FROM_HOST");
    descriptor.addMapping(fromHostMapping);

    DirectToFieldMapping paymentAmtMapping = new DirectToFieldMapping();
    paymentAmtMapping.setAttributeName("paymentAmt");
    paymentAmtMapping.setFieldName("PTRPAYMENT.PAYMENT_AMT");
    descriptor.addMapping(paymentAmtMapping);

    DirectToFieldMapping paymentDateTimeMapping = new DirectToFieldMapping();
    paymentDateTimeMapping.setAttributeName("paymentDateTime");
    paymentDateTimeMapping.setFieldName("PTRPAYMENT.PAYMENT_DATE_TIME");
    descriptor.addMapping(paymentDateTimeMapping);

    DirectToFieldMapping paymentIdMapping = new DirectToFieldMapping();
    paymentIdMapping.setAttributeName("paymentId");
    paymentIdMapping.setFieldName("PTRPAYMENT.PAYMENT_ID");
    descriptor.addMapping(paymentIdMapping);

    DirectToFieldMapping paymentProcessDateTimeMapping = new DirectToFieldMapping();
    paymentProcessDateTimeMapping.setAttributeName("paymentProcessDateTime");
    paymentProcessDateTimeMapping
        .setFieldName("PTRPAYMENT.PAYMENT_PROCESS_DATE_TIME");
    descriptor.addMapping(paymentProcessDateTimeMapping);

    OneToOneMapping accountMapping = new OneToOneMapping();
    accountMapping.setAttributeName("account");
    accountMapping
        .setReferenceClass(com.profitera.descriptor.db.account.Account.class);
    accountMapping.useBasicIndirection();
    accountMapping.addForeignKeyFieldName("PTRPAYMENT.ACCOUNT_ID",
        "PTRACCOUNT.ACCOUNT_ID");
    descriptor.addMapping(accountMapping);

    OneToOneMapping paymentProcessStatusRefMapping = new OneToOneMapping();
    paymentProcessStatusRefMapping.setAttributeName("paymentProcessStatusRef");
    paymentProcessStatusRefMapping
        .setReferenceClass(com.profitera.descriptor.db.reference.ProcessStatusRef.class);
    paymentProcessStatusRefMapping.useBasicIndirection();
    paymentProcessStatusRefMapping.addForeignKeyFieldName(
        "PTRPAYMENT.PAYMENT_PROCESS_STATUS",
        "PTRPROCESS_STATUS_REF.PROC_STATUS_ID");
    descriptor.addMapping(paymentProcessStatusRefMapping);

    OneToOneMapping paymentTypeRefMapping = new OneToOneMapping();
    paymentTypeRefMapping.setAttributeName("paymentTypeRef");
    paymentTypeRefMapping
        .setReferenceClass(com.profitera.descriptor.db.reference.PaymentTypeRef.class);
    paymentTypeRefMapping.useBasicIndirection();
    paymentTypeRefMapping.readOnly();
    paymentTypeRefMapping.addForeignKeyFieldName("PTRPAYMENT.PAYMENT_TYPE_ID",
        "PTRPAYMENT_TYPE_REF.PAY_TYPE_ID");
    descriptor.addMapping(paymentTypeRefMapping);

    OneToOneMapping tranCodeRefMapping = new OneToOneMapping();
    tranCodeRefMapping.setAttributeName("tranCodeRef");
    tranCodeRefMapping
        .setReferenceClass(com.profitera.descriptor.db.reference.TransactionCodeRef.class);
    tranCodeRefMapping.useBasicIndirection();
    tranCodeRefMapping.addForeignKeyFieldName("PTRPAYMENT.TRAN_CODE_ID",
        "PTRTRAN_CODE_REF.TRAN_CODE_ID");
    descriptor.addMapping(tranCodeRefMapping);
    return descriptor;
  }

  public Descriptor buildPaymentCashDescriptor() {
    Descriptor descriptor = new Descriptor();
    descriptor.setJavaClass(com.profitera.descriptor.db.payment.PaymentCash.class);
    descriptor.getInheritancePolicy().setParentClass(com.profitera.descriptor.db.payment.Payment.class);
    descriptor.setAlias("PtrpaymentCash");
    descriptor.getQueryManager().checkCacheForDoesExist();
    return descriptor;
  }

  public Descriptor buildPaymentCheckDescriptor() {
    Descriptor descriptor = new Descriptor();
    descriptor.setJavaClass(com.profitera.descriptor.db.payment.PaymentCheck.class);
    descriptor.getInheritancePolicy().setParentClass(com.profitera.descriptor.db.payment.Payment.class);
    descriptor.setAlias("PtrpaymentCheck");
    descriptor.getQueryManager().checkCacheForDoesExist();
    return descriptor;
  }

  public Descriptor buildPaymentDebitAdjustmentDescriptor() {
    Descriptor descriptor = new Descriptor();
    descriptor.setJavaClass(com.profitera.descriptor.db.payment.PaymentDebitAdjustment.class);
    descriptor.getInheritancePolicy().setParentClass(com.profitera.descriptor.db.payment.Payment.class);
    descriptor.setAlias("PtrpaymentDebitAdjustment");
    descriptor.getQueryManager().checkCacheForDoesExist();
    return descriptor;
  }
}
