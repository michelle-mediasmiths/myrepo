package com.mediasmiths.foxtel.ip.mayam.pdp;

public enum FoxtelGroups
{
	GS_MAM_FULL_Ordering("GS-MAM_FULL_Ordering"),
	GS_MAM_GE_Programmer("GS-MAM_GE_Programmer"),
	GS_MAM_LS_Programmer("GS-MAM_LS_Programmer"),
	GS_MAM_FC_Programmer("GS-MAM_FC_Programmer"),
	GS_MAM_OD_Programmer("GS-MAM_OD_Programmer"),
	GS_MAM_MV_Programmer("GS-MAM_MV_Programmer"),
	GS_MAM_ME_Programmer("GS-MAM_ME_Programmer"),
	GS_MAM_GE_Publisher("GS-MAM_GE_Publisher"),
	GS_MAM_LS_Publisher("GS-MAM_LS_Publisher"),
	GS_MAM_FC_Publisher("GS-MAM_FC_Publisher"),
	GS_MAM_OD_Publisher("GS-MAM_OD_Publisher"),
	GS_MAM_MV_Publisher("GS-MAM_MV_Publisher"),
	GS_MAM_ME_Publisher("GS-MAM_ME_Publisher"),
	GS_MAM_GE_Restricted("GS-MAM_GE_Restricted"),
	GS_MAM_LS_Restricted("GS-MAM_LS_Restricted"),
	GS_MAM_FC_Restricted("GS-MAM_FC_Restricted"),
	GS_MAM_OD_Restricted("GS-MAM_OD_Restricted"),
	GS_MAM_MV_Restricted("GS-MAM_MV_Restricted"),
	GS_MAM_ME_Restricted("GS-MAM_ME_Restricted"),
	GS_MAM_GE_Viewer("GS-MAM_GE_Viewer"),
	GS_MAM_LS_Viewer("GS-MAM_LS_Viewer"),
	GS_MAM_FC_Viewer("GS-MAM_FC_Viewer"),
	GS_MAM_OD_Viewer("GS-MAM_OD_Viewer"),
	GS_MAM_MV_Viewer("GS-MAM_MV_Viewer"),
	GS_MAM_ME_Viewer("GS-MAM_ME_Viewer"),
	GS_MAM_GE_Promo("GS-MAM_GE_Promo"),
	GS_MAM_LS_Promo("GS-MAM_LS_Promo"),
	GS_MAM_FC_Promo("GS-MAM_FC_Promo"),
	GS_MAM_OD_Promo("GS-MAM_OD_Promo"),
	GS_MAM_MV_Promo("GS-MAM_MV_Promo"),
	GS_MAM_ME_Promo("GS-MAM_ME_Promo"),
	GS_MAM_FULL_Preview("GS-MAM_FULL_Preview"),
	GS_MAM_FULL_Classification("GS-MAM_FULL_Classification"),
	GS_MAM_FULL_BOPS("GS-MAM_FULL_BOPS"),
	GS_MAM_FULL_Editing("GS-MAM_FULL_Editing"),
	GS_MAM_FULL_Conforming("GS-MAM_FULL_Conforming"),
	GS_MAM_FULL_Publicity("GS-MAM_FULL_Publicity"),
	GS_MAM_AO_Ordering("GS-MAM_AO_Ordering"),
	GS_MAM_AO_Programmer("GS-MAM_AO_Programmer"),
	GS_MAM_AO_Viewer("GS-MAM_AO_Viewer"),
	GS_MAM_AO_Promo("GS-MAM_AO_Promo"),
	GS_MAM_AO_Preview("GS-MAM_AO_Preview"),
	GS_MAM_AO_Classification("GS-MAM_AO_Classification"),
	GS_MAM_AO_BOPS("GS-MAM_AO_BOPS"),
	GS_MAM_AO_Editing("GS-MAM_AO_Editing"),
	GS_MAM_AO_Conforming("GS-MAM_AO_Conforming"),
	GS_MAM_AO_Publicity("GS-MAM_AO_Publicity"),
	GS_MAM_FULL_SysAdmin("GS-MAM_FULL_SysAdmin"),
	GS_MAM_FULL_SuperUser("GS-MAM_FULL_SuperUser"),
	GS_MAM_AO_SuperUser("GS-MAM_AO_SuperUser"),
	GS_MAM_FULL_TechAdmin("GS-MAM_FULL_TechAdmin"),
	GS_MAM_AO_TechAdmin("GS-MAM_AO_TechAdmin"),
	admin("admin"),
	ardendo("ardendo"),
	Test_Full_Class("Test_Full_Class"),
	GS_MAM_FULL_Class("GS-MAM_FULL_Class"),
	GS_MAM_AO("GS-MAM_AO");

	private final String text;

	FoxtelGroups(String text)
	{
		this.text = text;
	}

	public String toSting()
	{
		return text;
	}
	
	public static FoxtelGroups fromString(String s)
	{
		if (s == null)
		{
			return valueOf(s);
		}
		else
		{
			return valueOf(s.replace('-', '_'));
		}
	}
}
