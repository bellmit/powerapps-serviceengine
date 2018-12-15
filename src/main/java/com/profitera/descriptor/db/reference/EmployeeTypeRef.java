// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.reference;

public class EmployeeTypeRef
	implements java.io.Serializable
{
	// Generated constants
	public static final String DISABLE = "disable";
	public static final String EMPLOYEE = "employee";
	public static final String EMPLOYEE_TYPE_CODE = "employeeTypeCode";
	public static final String EMPLOYEE_TYPE_DESC = "employeeTypeDesc";
	public static final String EMPLOYEE_TYPE_ID = "employeeTypeId";
	public static final String SORT_PRIORITY = "sortPriority";
	// End of generated constants
	
	private java.lang.Double disable;

	private oracle.toplink.indirection.ValueHolderInterface employee= new oracle.toplink.indirection.ValueHolder(new java.util.Vector());

	private java.lang.String employeeTypeCode;

	private java.lang.String employeeTypeDesc;

	private java.lang.Double employeeTypeId;

	private java.lang.Double sortPriority;

	public  EmployeeTypeRef()
	{
		// Fill in method body here.
	}

	public java.lang.Double getDisable()
	{
		return disable;
	}

	public java.util.Vector getEmployee()
	{
		return (java.util.Vector) employee.getValue();
	}

	public java.lang.String getEmployeeTypeCode()
	{
		return employeeTypeCode;
	}

	public java.lang.String getEmployeeTypeDesc()
	{
		return employeeTypeDesc;
	}

	public java.lang.Double getEmployeeTypeId()
	{
		return employeeTypeId;
	}

	public java.lang.Double getSortPriority()
	{
		return sortPriority;
	}

	public void setDisable(java.lang.Double disable)
	{
		this.disable = disable;
	}

	public void setEmployee(java.util.Vector employee)
	{
		this.employee.setValue(employee);
	}

	public void setEmployeeTypeCode(java.lang.String employeeTypeCode)
	{
		this.employeeTypeCode = employeeTypeCode;
	}

	public void setEmployeeTypeDesc(java.lang.String employeeTypeDesc)
	{
		this.employeeTypeDesc = employeeTypeDesc;
	}

	public void setEmployeeTypeId(java.lang.Double employeeTypeId)
	{
		this.employeeTypeId = employeeTypeId;
	}

	public void setSortPriority(java.lang.Double sortPriority)
	{
		this.sortPriority = sortPriority;
	}
}
