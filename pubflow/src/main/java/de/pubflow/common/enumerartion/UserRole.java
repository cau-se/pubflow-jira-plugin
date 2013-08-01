package de.pubflow.common.enumerartion;

public enum UserRole {
	
	GUEST,
	USER,
	DATAMANAGER,
	ADMIN,
	DEVELOPER;
	
	public static UserRole loadFromString()
	{
		return UserRole.GUEST;
	}
}
