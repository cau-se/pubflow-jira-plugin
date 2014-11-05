package de.pubflow.server.common.entity;

import de.pubflow.server.common.enumeration.UserRole;

public class User implements StringSerializable{

	private UserRole role;
	
	private String title;
	private String prename;
	private String surname;
	private String mailAdress;
	private Institute institute;
	private String phoneNumber;
	
	private String publicKey; 
	/**
	 * @return the publicKey
	 */
	public String getPublicKey() {
		return publicKey;
	}

	/**
	 * @param publicKey the publicKey to set
	 */
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	/**
	 * @return the role
	 */
	public UserRole getRole() {
		return role;
	}

	/**
	 * @param role the role to set
	 */
	public void setRole(UserRole role) {
		this.role = role;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the prename
	 */
	public String getPrename() {
		return prename;
	}

	/**
	 * @param prename the prename to set
	 */
	public void setPrename(String prename) {
		this.prename = prename;
	}

	/**
	 * @return the surname
	 */
	public String getSurname() {
		return surname;
	}

	/**
	 * @param surname the surname to set
	 */
	public void setSurname(String surname) {
		this.surname = surname;
	}

	/**
	 * @return the mailAdress
	 */
	public String getMailAdress() {
		return mailAdress;
	}

	/**
	 * @param mailAdress the mailAdress to set
	 */
	public void setMailAdress(String mailAdress) {
		this.mailAdress = mailAdress;
	}

	/**
	 * @return the institute
	 */
	public Institute getInstitute() {
		return institute;
	}

	/**
	 * @param institute the institute to set
	 */
	public void setInstitute(Institute institute) {
		this.institute = institute;
	}

	/**
	 * @return the phoneNumber
	 */
	public String getPhoneNumber() {
		return phoneNumber;
	}

	/**
	 * @param phoneNumber the phoneNumber to set
	 */
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	@Override
	public String transformToString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initFromString(String content) {
		// TODO Auto-generated method stub
		
	}
	
	public static User getUserFromJiraID(String value)
	{
		//TODO
		return null;
	}

}
