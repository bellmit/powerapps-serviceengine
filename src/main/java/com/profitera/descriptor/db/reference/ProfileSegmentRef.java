// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class ProfileSegmentRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String DISABLE = "disable";
	public static final String MINIMUM_SCORE = "minimumScore";
	public static final String PROFILE_CODE = "profileCode";
	public static final String PROFILE_DESC = "profileDesc";
	public static final String PROFILE_ID = "profileId";
	public static final String RECOVERY_POTENTIAL = "recoveryPotential";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.Double disable;

	private java.lang.Double minimumScore;

	private java.lang.String profileCode;

	private java.lang.String profileDesc;

	private java.lang.Double profileId;

	private java.lang.Double recoveryPotential;

	private java.lang.Double sortPriority;

	public  ProfileSegmentRef()
	{
		// Fill in method body here.
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.lang.Double getMinimumScore()
	{
		return minimumScore;
	}

	public java.lang.String getProfileCode()
	{
		return profileCode;
	}

	public java.lang.String getProfileDesc()
	{
		return profileDesc;
	}

	public java.lang.Double getProfileId()
	{
		return profileId;
	}

	public java.lang.Double getRecoveryPotential()
	{
		return recoveryPotential;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setMinimumScore(java.lang.Double minimumScore)
	{
		this.minimumScore = minimumScore;
	}

	public void setProfileCode(java.lang.String profileCode)
	{
		this.profileCode = profileCode;
	}

	public void setProfileDesc(java.lang.String profileDesc)
	{
		this.profileDesc = profileDesc;
	}

	public void setProfileId(java.lang.Double profileId)
	{
		this.profileId = profileId;
	}

	public void setRecoveryPotential(java.lang.Double recoveryPotential)
	{
		this.recoveryPotential = recoveryPotential;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}
}
