/**
 * 
 */
package com.profitera.services.system.loan.impl;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public interface IInstallmentProvider {
  List fetchNextInstallments(Date lastDate) throws SQLException;
}