/*
 * Created on Aug 29, 2003
 */
package com.profitera.descriptor.business.reference;

import com.profitera.descriptor.db.account.CustomerSegment;
import com.profitera.descriptor.db.reference.*;
import com.profitera.descriptor.db.worklist.WorkList;
import com.profitera.util.PrimitiveValue;

/**
 * All the convertToBusinessBean methods in the this class 
 * SHOULD <b>NOT</b> throw null pointer exceptions, always just
 * return null. Makes for clean services code with all those optional
 * refs out there.
 * @author jamison
 *
 */
public class ReferenceBeanConverter {

    public static ProfileSegmentRefBusinessBean convertToBusinessBean(ProfileSegmentRef dbDescriptor) {
    	if (dbDescriptor == null) return null;
        ProfileSegmentRefBusinessBean bean = new ProfileSegmentRefBusinessBean();
        bean.setId(dbDescriptor.getProfileId());
        bean.setCode(dbDescriptor.getProfileCode());
        bean.setDesc(dbDescriptor.getProfileDesc());
        bean.setRecoveryPotential(dbDescriptor.getRecoveryPotential());
        bean.setMinimumScore(dbDescriptor.getMinimumScore());
        return bean;
    }

    public static RiskLevelRefBusinessBean convertToBusinessBean(RiskLevelRef dbDescriptor) {
		if (dbDescriptor == null) return null;
        RiskLevelRefBusinessBean bean = new RiskLevelRefBusinessBean();
        bean.setId(dbDescriptor.getRiskLevelId());
        bean.setCode(dbDescriptor.getRiskLevelCode());
        bean.setDesc(dbDescriptor.getRiskLevelDesc());
        bean.setMinimumScore(dbDescriptor.getMinimumScore());
        return bean;
    }

    public static ProfileSegmentRef convertToDBDescriptor(ProfileSegmentRefBusinessBean bean, ProfileSegmentRef registeredDbDescriptor) {
        if (bean == null) return null;
        registeredDbDescriptor.setProfileId(bean.getId());
        registeredDbDescriptor.setProfileCode(bean.getCode());
        registeredDbDescriptor.setProfileDesc(bean.getDesc());
        registeredDbDescriptor.setRecoveryPotential(bean.getRecoveryPotential());
        registeredDbDescriptor.setMinimumScore(bean.getMinimumScore());
        registeredDbDescriptor.setDisable(bean.isDisabled() ? new Double(1) : new Double(0));
        registeredDbDescriptor.setSortPriority(new Double(bean.getSortPriority()));
        return registeredDbDescriptor;
    }

    public static ReferenceBusinessBean createReferenceBusinessBean(AccountSexRef ref) {
        if (ref == null) return null;
        return new ReferenceBusinessBean(ref.getAccSexId(), ref.getAccSexCode(), ref.getAccSexDesc());
    }

    public static TreatProcessTypeStatusRef convertToDBDescriptor(
        TreatmentProcessTypeStatusRefBusinessBean bean,
        TreatProcessTypeStatusRef registeredDbDescriptor) {
        registeredDbDescriptor.setTreatprocTypeStatusId(bean.getId());
        registeredDbDescriptor.setTreatprocTypeStatusCode(bean.getCode());
        registeredDbDescriptor.setTreatprocTypeStatusDesc(bean.getDesc());
        registeredDbDescriptor.setTreatprocTypeId(bean.getTreatmentProcessTypeId());
        registeredDbDescriptor.setTreatprocStatusId(bean.getTreatmentProcessStatusId());
        registeredDbDescriptor.setSortPriority(new Double(bean.getSortPriority()));
        registeredDbDescriptor.setDisable(new Double(bean.isDisabled() ? 1 : 0));
        return registeredDbDescriptor;
    }

    public static TreatmentProcessTypeStatusRefBusinessBean createReferenceBusinessBean(TreatProcessTypeStatusRef ref) {
      TreatmentProcessTypeStatusRefBusinessBean r = new TreatmentProcessTypeStatusRefBusinessBean(
            ref.getTreatprocTypeStatusId(),
            ref.getTreatprocTypeId(),
            ref.getTreatprocStatusId(),
            ref.getTreatprocTypeStatusCode(),
            ref.getTreatprocTypeStatusDesc());
      r.setSortPriority(PrimitiveValue.intValue(ref.getSortPriority(), 0));
      r.setDisabled(ref.getDisable().intValue() == 1);
      return r;
    }

    public static ReferenceBusinessBean convertToBusinessBean(BlockCodeRef ref) {
        if (ref == null) return null;
        return new ReferenceBusinessBean(ref.getBlockCodeId(), ref.getBlockCodeCode(), ref.getBlockCodeDesc());
    }

    public static ReferenceBusinessBean convertToBusinessBean(WorkList ref) {
        if (ref == null) return null;
        ReferenceBusinessBean r = new ReferenceBusinessBean(ref.getWorkListId(), ref.getWorkListName(), ref.getWorkListDesc());
        r.setSortPriority(PrimitiveValue.intValue(ref.getSortPriority(), 0));
        r.setDisabled(PrimitiveValue.intValue(ref.getDisable(), 0) == 1);
        return r;
    }

    public static ReferenceBusinessBean convertToBusinessBean(TreatmentStageRef ref) {
    	if (ref == null) return null;
        return new ReferenceBusinessBean(ref.getTreatmentStageId(), ref.getTreatmentStageCode(), ref.getTreatmentStageDesc());
    }

    public static RiskLevelRef convertToDBDescriptor(RiskLevelRefBusinessBean bean, RiskLevelRef registeredDbDescriptor) {
        registeredDbDescriptor.setRiskLevelId(bean.getId());
        registeredDbDescriptor.setRiskLevelCode(bean.getCode());
        registeredDbDescriptor.setRiskLevelDesc(bean.getDesc());
        registeredDbDescriptor.setMinimumScore(bean.getMinimumScore());
        registeredDbDescriptor.setDisable(bean.isDisabled() ? new Double(1) : new Double(0));
        registeredDbDescriptor.setSortPriority(new Double(bean.getSortPriority()));
        return registeredDbDescriptor;
    }

    public static TreatmentProcessSubtypeRefBusinessBean convertToBusinessBean(TreatprocSubtypeRef ref) {
		if (ref == null) return null;
        return new TreatmentProcessSubtypeRefBusinessBean(
            ref.getTreatprocSubtypeId(),
            ref.getTreatprocTypeId(),
            ref.getTreatprocTypeCode(),
            ref.getTreatprocTypeDesc());
    }

    public static ReferenceBusinessBean convertToBusinessBean(PaymentLocationRef ref) {
		if (ref == null) return null;
        ReferenceBusinessBean _bean = new ReferenceBusinessBean(ref.getPaymentLocationId(), ref.getLocationCategory(), ref.getLocationName());
        return _bean;
    }

    public static ReferenceBusinessBean convertToBusinessBean(UserRoleRef ref) {
		if (ref == null) return null;
        ReferenceBusinessBean _bean = new ReferenceBusinessBean();
        _bean.setDesc(ref.getRoleDesc());
        _bean.setCode(ref.getRoleName());
        _bean.setId(ref.getRoleId());
        return _bean;
    }

    public static UomMeasureRefBusinessBean convertToBusinessBean(UomMeasureRef ref) {
		if (ref == null) return null;
		UomMeasureRefBusinessBean bean = new UomMeasureRefBusinessBean();
		bean.setUomCategory(ref.getUomCategory());
		bean.setUomId(ref.getUomId());
		bean.setUomType(ref.getUomType());
		bean.setUomValue(ref.getUomValue());
		return bean;
	}

	/**
	 * @param ref
	 * @return
	 */
	public static ReferenceBusinessBeanIntf convertToBusinessBean(EmployerBusinessRef ref) {
		if (ref == null) return null;
		return new ReferenceBusinessBean(ref.getEmployerBusinessId(), ref.getEmployerBusinessCode(), ref.getEmployerBusinessDesc());
	}

	/**
	 * @param ref
	 * @return
	 */
	public static ReferenceBusinessBeanIntf convertToBusinessBean(EmploymentTypeRef ref) {
		if (ref == null) return null;
		return new ReferenceBusinessBean(ref.getEmploymentTypeId(), ref.getEmploymentTypeCode(), ref.getEmploymentTypeDesc());
	}

	/**
	 * @param ref
	 * @return
	 */
	public static ReferenceBusinessBeanIntf convertToBusinessBean(AccountWorkListStatusRef ref) {
		if (ref == null) return null;
		return new ReferenceBusinessBean(ref.getAccWrklistStatusId(), ref.getAccWrklistStatusCode(), ref.getAccWrklistStatusDesc());
	}

	/**
	 * @param ref
	 * @return
	 */
	public static TreatmentProcessTypeStatusRefBusinessBean convertToBusinessBean(TreatProcessTypeStatusRef ref) {
		if (ref == null) return null;
		TreatmentProcessTypeStatusRefBusinessBean b = new TreatmentProcessTypeStatusRefBusinessBean();
		b.setId(ref.getTreatprocTypeStatusId());
		b.setCode(ref.getTreatprocTypeStatusCode());
		b.setDesc(ref.getTreatprocTypeStatusDesc());
		b.setTreatmentProcessStatusId(ref.getTreatprocStatusId());
		b.setTreatmentProcessTypeId(ref.getTreatprocTypeId());
		b.setSortPriority(PrimitiveValue.intValue(ref.getSortPriority(),0));
		b.setDisabled(PrimitiveValue.intValue(ref.getDisable(),0) == 1);
		return b;
	}

	/**
	 * @param ref
	 * @return
	 */
	public static ReferenceBusinessBeanIntf convertToBusinessBean(AccountStatusRef ref) {
		if (ref == null) return null;
		return new ReferenceBusinessBean(ref.getAccountStatusId(), ref.getAccountStatusCode(), ref.getAccountStatusDesc());
	}

	/**
	 * @param ref
	 * @return
	 */
	public static ReferenceBusinessBeanIntf convertToBusinessBean(AutoPayRef ref) {
		if (ref == null) return null;
		return new ReferenceBusinessBean(ref.getAutoPayId(), ref.getAutoPayCode(), ref.getAutoPayDesc());
	}

	/**
	 * @param ref
	 * @return
	 */
	public static ReferenceBusinessBeanIntf convertToBusinessBean(BillingCycleRef ref) {
		if (ref == null) return null;
		return new ReferenceBusinessBean(ref.getBillingCycleId(), ref.getBillingCycleCode(), ref.getBillingCycleDesc());
	}

	/**
	 * @param ref
	 * @return
	 */
	public static ReferenceBusinessBeanIntf convertToBusinessBean(CampaignCodeRef ref) {
		if (ref == null) return null;
		return new ReferenceBusinessBean(ref.getCampaignCodeId(), ref.getCampaignCodeCode(), ref.getCampaignCodeDesc());
	}

	/**
	 * @param ref
	 * @return
	 */
	public static ReferenceBusinessBeanIntf convertToBusinessBean(ChannelCodeRef ref) {
		if (ref == null) return null;
		return new ReferenceBusinessBean(ref.getChannelCodeId(), ref.getChannelCodeCode(), ref.getChannelCodeDesc());
	}

	/**
	 * @param ref
	 * @return
	 */
	public static ReferenceBusinessBeanIntf convertToBusinessBean(ChargeOffReasonRef ref) {
		if (ref == null) return null;
		return new ReferenceBusinessBean(ref.getChargeoffReasonId(), ref.getChargeoffReasonCode(), ref.getChargeoffReasonDesc());
	}

	/**
	 * @param ref
	 * @return
	 */
	public static ReferenceBusinessBeanIntf convertToBusinessBean(ChargeOffStatusRef ref) {
		if (ref == null) return null;
		return new ReferenceBusinessBean(ref.getChargeoffStatusId(), ref.getChargeoffStatusCode(), ref.getChargeoffStatusDesc());
	}

	/**
	 * @param ref
	 * @return
	 */
	public static ReferenceBusinessBeanIntf convertToBusinessBean(CollectionStatusRef ref) {
		if (ref == null) return null;
		return new ReferenceBusinessBean(ref.getCollStatusId(), ref.getCollStatusCode(), ref.getCollStatusDesc());
	}

	/**
	 * @param ref
	 * @return
	 */
	public static ReferenceBusinessBeanIntf convertToBusinessBean(CollectionReasonRef ref) {
		if (ref == null) return null;
		return new ReferenceBusinessBean(ref.getCollectionReasonId(), ref.getCollectionReasonCode(), ref.getCollectionReasonDesc());
	}

	/**
	 * @param ref
	 * @return
	 */
	public static ReferenceBusinessBeanIntf convertToBusinessBean(CustomerSegment ref) {
		if (ref == null) return null;
		return new ReferenceBusinessBean(ref.getCustomerSegmentId(), ref.getCustomerSegmentCode(), ref.getCustSegmentDesc());
	}

	/**
	 * @param ref
	 * @return
	 */
	public static ReferenceBusinessBeanIntf convertToBusinessBean(DebtRecoveryStatusRef ref) {
		if (ref == null) return null;
		return new ReferenceBusinessBean(ref.getDebtrecStatusId(), ref.getDebtrecStatusCode(), ref.getDebtrecStatusDesc());
	}

	/**
	 * @param ref
	 * @return
	 */
	public static ReferenceBusinessBeanIntf convertToBusinessBean(DelinquencyTypeRef ref) {
		if (ref == null) return null;
		return new ReferenceBusinessBean(ref.getDelinquencyTypeId(), ref.getDelinquencyTypeCode(), ref.getDelinquencyTypeDesc());
	}

	/**
	 * @param ref
	 * @return
	 */
	public static ReferenceBusinessBeanIntf convertToBusinessBean(PaymentBehaviourRef ref) {
		if (ref == null) return null;
		return new ReferenceBusinessBean(ref.getBehaviourId(), ref.getBehaviourCode(), ref.getBehaviourDesc());
	}

	/**
	 * @param ref
	 * @return
	 */
	public static ReferenceBusinessBeanIntf convertToBusinessBean(PaymentFrequencyRef ref) {
		if (ref == null) return null;
		return new ReferenceBusinessBean(ref.getPaymentFrequencyId(), ref.getPaymentFrequencyCode(), ref.getPaymentFrequencyDesc());
	}

	/**
	 * @param ref
	 * @return
	 */
	public static ReferenceBusinessBeanIntf convertToBusinessBean(ProductTypeRef ref) {
		if (ref == null) return null;
		return new ReferenceBusinessBean(ref.getProductTypeId(), ref.getProductTypeCode(), ref.getProductTypeDesc());
	}

	/**
	 * @param ref
	 * @return
	 */
	public static ReferenceBusinessBeanIntf convertToBusinessBean(SensitiveStatusRef ref) {
		if (ref == null) return null;
		return new ReferenceBusinessBean(ref.getSenseStatusId(), ref.getSenseStatusCode(), ref.getSenseStatusDesc());
	}

	/**
	 * @param ref
	 * @return
	 */
	public static TreatmentStreamReferenceBusinessBean convertToBusinessBean(TreatmentStreamRef ref) {
		if (ref == null) return null;
		if (ref.getTreatmentStageRef() == null) throw new RuntimeException("Stage is null for stream: " + ref.getTreatmentStreamDesc());
		return new TreatmentStreamReferenceBusinessBean(ref.getTreatmentStreamId(), 
				ref.getTreatmentStreamCode(), 
				ref.getTreatmentStreamDesc(), 
				convertToBusinessBean(ref.getTreatmentStageRef()),
				PrimitiveValue.intValue(ref.getDisable(), 0) != 0,
				PrimitiveValue.intValue(ref.getSortPriority(), 0));
	}

	/**
	 * @param ref
	 * @return
	 */
	public static ReferenceBusinessBean convertToBusinessBean(LegalReasonRef ref) {
		if (ref == null) return null;
		return new ReferenceBusinessBean(ref.getLegalReasonId(), ref.getLegalReasonCode(), ref.getLegalReasonDesc());
	}

	/**
	 * @param ref
	 * @return
	 */
	public static ReferenceBusinessBean convertToBusinessBean(TemplateTypeRef ref) {
		if (ref == null) return null;
		return new ReferenceBusinessBean(ref.getTemplateTypeId(), ref.getTemplateTypeCode(), ref.getTemplateTypeDesc());
	}

	/**
	 * @param ref
	 * @return
	 */
	public static ReferenceBusinessBean convertToBusinessBean(AgencyTypeRef ref) {
		if (ref == null) return null;
		return new ReferenceBusinessBean(ref.getAgyTypeId(), ref.getAgyTypeCode(), ref.getAgyTypeDesc());
	}

  public static ReferenceBusinessBean convertToBusinessBean(NotifierCodeRef n) {
    ReferenceBusinessBean b = new ReferenceBusinessBean(new Double(n.getId().doubleValue()), n.getCode(), n.getDescription());
    b.setSortPriority(PrimitiveValue.intValue(n.getSortPriority(), 0));
    b.setDisabled(PrimitiveValue.intValue(n.getDisable(), 0) == 1);
    return b;
  }
	
	
}
