package com.profitera.services.business.legalpanel;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.deployment.rmi.LegalPanelServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.dataaccess.MapVerifyingMapCar;

public class LegalPanelService extends ProviderDrivenService implements
		LegalPanelServiceIntf {

    private Map getDefendant(Long id) throws TransferObjectException {
    	final IReadWriteDataProvider p = getReadWriteProvider();
    	Map m = new HashMap();
    	m.put("ID", id);
        List l = executeListQuery(IReadOnlyDataProvider.LIST_RESULTS, "getDefendant", m, new MapVerifyingMapCar(new String[]{"ID"}), p);
        if (l.size() > 0)
          return (Map) l.get(0);
        return null;
      }
	
	public TransferObject addDefendant(final Map bean, Long id) {
		final IReadWriteDataProvider p = getReadWriteProvider();
		IRunnableTransaction rt = new IRunnableTransaction(){
			public void execute(ITransaction t) throws SQLException, AbortTransactionException{
				p.insert("insertDefendant", bean, t);
			}
		};
		try {
			p.execute(rt);
		} catch(AbortTransactionException e){
			return returnFailWithTrace("Insert failed", "insertDefendant", "insert", bean, e);
		} catch(SQLException e){
			return returnFailWithTrace("Insert failed", "insertDefendant", "insert", bean, e);
		}
		return new TransferObject(bean);
	}

	public TransferObject updateDefendant(final Map bean) {
		final IReadWriteDataProvider p = getReadWriteProvider();
		IRunnableTransaction rt = new IRunnableTransaction(){
			public void execute(ITransaction t) throws SQLException, AbortTransactionException{
				p.update("updateDefendant", bean, t);
			}
		};
		try {
			p.execute(rt);
			return new TransferObject(getDefendant((Long)bean.get("ID")));
		} catch(AbortTransactionException e){
			return sqlFailure("update", "updateDefendant", bean, e);
		} catch(SQLException e) {
			return sqlFailure("update", "updateDefendant", bean, e);
		} catch(TransferObjectException e){
			return e.getTransferObject();
		}
	}
}
