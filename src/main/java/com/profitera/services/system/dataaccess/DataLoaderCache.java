/*
 * Created on Sep 26, 2003
 */
package com.profitera.services.system.dataaccess;

import com.profitera.descriptor.db.reference.AccountSexRef;
import com.profitera.descriptor.db.reference.AutoPayRef;
import com.profitera.descriptor.db.reference.AccountRelationshipRef;
import com.profitera.descriptor.db.reference.AgentBankRef;
import com.profitera.descriptor.db.reference.BillingCycleRef;
import com.profitera.descriptor.db.reference.BlockCodeRef;
import com.profitera.descriptor.db.reference.CampaignCodeRef;
import com.profitera.descriptor.db.reference.ChannelCodeRef;
import com.profitera.descriptor.db.reference.ChargeOffReasonRef;
import com.profitera.descriptor.db.reference.CitizenshipRef;
import com.profitera.descriptor.db.reference.CollectionReasonRef;
import com.profitera.descriptor.db.reference.ContactTypeRef;
import com.profitera.descriptor.db.reference.DebtRecoveryStatusRef;
import com.profitera.descriptor.db.reference.EmployeeTypeRef;
import com.profitera.descriptor.db.reference.MaritalStatusRef;
import com.profitera.descriptor.db.reference.OccupationRef;
import com.profitera.descriptor.db.reference.PremisesRef;
import com.profitera.descriptor.db.reference.ProductTypeRef;
import com.profitera.descriptor.db.reference.RaceTypeRef;
import com.profitera.descriptor.db.reference.UserCode3Ref;
import com.profitera.descriptor.db.user.BusinessUnit;
import com.profitera.descriptor.db.user.User;

/**
 * @author jamison
 *
 */
public class DataLoaderCache extends FullTableCache{
    private static Class[] CLASSES =
        {
            EmployeeTypeRef.class,
            User.class,
            DebtRecoveryStatusRef.class,
            ProductTypeRef.class,
            BlockCodeRef.class,
            BillingCycleRef.class,
            MaritalStatusRef.class,
            AccountSexRef.class,
            RaceTypeRef.class,
            CitizenshipRef.class,
            PremisesRef.class,
            ContactTypeRef.class,
            BusinessUnit.class,
            CampaignCodeRef.class,
            CollectionReasonRef.class,
            AutoPayRef.class,
            AccountRelationshipRef.class,
            AgentBankRef.class,
            ChannelCodeRef.class,
            ChargeOffReasonRef.class,
            OccupationRef.class,
            UserCode3Ref.class
            };

    private static String[] METHODS =
        {
            "getEmployeeTypeCode",
            "getUserId",
            "getDebtrecStatusCode",
            "getProductTypeCode",
            "getBlockCodeCode",
            "getBillingCycleCode",
            "getMaritalStatusRefCode",
            "getAccSexCode",
            "getRaceTypeCode",
            "getCitizenshipCode",
            "getPremisesCode",
            "getContactTypeCode",
            "getBranchId",
            "getCampaignCodeCode",
            "getCollectionReasonCode",
            "getAutoPayCode",
            "getRelTypeCode",//AccountRelationType
            "getRelTypeCode",//AgentBank
            "getChannelCodeCode",
            "getChargeoffReasonCode",
            "getOccupationCode",
            "getCode"
    	};

    public DataLoaderCache() {
		super(CLASSES,METHODS);    	
    }

    public EmployeeTypeRef getEmployeeType(String code) {
        return (EmployeeTypeRef) getObject(EmployeeTypeRef.class,code);

    }
    public User getUser(String collector) {
		return (User) getObject(User.class,collector);
    }

    public DebtRecoveryStatusRef getDebtRecoveryStatus(String status) {
        return (DebtRecoveryStatusRef) getObject(DebtRecoveryStatusRef.class,status);
    }

    public ProductTypeRef getProductType(String cardType) {
        return (ProductTypeRef) getObject(ProductTypeRef.class,cardType);
    }

    public BlockCodeRef getBlockCode(String blkCode) {
        return (BlockCodeRef) getObject(BlockCodeRef.class,blkCode);
    }

    public BillingCycleRef getBillingCycle(String cycle) {
        return (BillingCycleRef) getObject(BillingCycleRef.class,cycle);
    }

    public MaritalStatusRef getMaritalStatus(String maritalStat) {
        return (MaritalStatusRef) getObject(MaritalStatusRef.class,maritalStat);
    }

    public AccountSexRef getAccountSex(String sex) {
        return (AccountSexRef) getObject(AccountSexRef.class,sex);
    }

    public RaceTypeRef getRaceType(String race) {
        return (RaceTypeRef) getObject(RaceTypeRef.class,race);
    }

    public CitizenshipRef getCitizenship(String citizen) {
        return (CitizenshipRef) getObject(CitizenshipRef.class,citizen);
    }

    public PremisesRef getPremises(String premises) {
        return (PremisesRef) getObject(PremisesRef.class,premises);
    }

    public ContactTypeRef getContactType(String type) {
        return (ContactTypeRef) getObject(ContactTypeRef.class,type);
    }

    public BusinessUnit getBusinessUnit(String branchCode) {
        return (BusinessUnit) getObject(BusinessUnit.class,branchCode);
    }

    public CampaignCodeRef getCampaignCode(String campaignCode) {
        return (CampaignCodeRef) getObject(CampaignCodeRef.class,campaignCode);
    }

    public CollectionReasonRef getCollectionReason(String reason) {
        return (CollectionReasonRef) getObject(CollectionReasonRef.class,reason);
    }

    public AutoPayRef getAutoPay(String si) {
        return (AutoPayRef) getObject(AutoPayRef.class,si);
    }

    public AccountRelationshipRef getAccountRelationshipRef(String relationshipTypeID) {
        return (AccountRelationshipRef) getObject(AccountRelationshipRef.class,relationshipTypeID);
    }    

    public AgentBankRef getAgentBankRef(String agentBank){
        return (AgentBankRef) getObject(AgentBankRef.class,agentBank);    	
    }
    
    public ChannelCodeRef getChannelCode(String channelCode) {
		return (ChannelCodeRef) getObject(ChannelCodeRef.class,channelCode);
    }

    public ChargeOffReasonRef getChargeOffReason(String chargeOff) {
		return (ChargeOffReasonRef) getObject(ChargeOffReasonRef.class,chargeOff);
    }

	/**
	 * @param occupation
	 * @return
	 */
	public OccupationRef getOccupation(String occupation) {
		return (OccupationRef) getObject(OccupationRef.class, occupation);
	}

  public UserCode3Ref getUserCode3(String userCode3) {
    return (UserCode3Ref) getObject(UserCode3Ref.class, userCode3);
  }

}
