package com.sdk.integration;

import com.sdk.integration.shared.Constants;
import com.sdk.wialon.core.Errors;
import com.sdk.wialon.core.Session;
import com.sdk.wialon.extra.SearchSpec;
import com.sdk.wialon.item.Item;
import com.sdk.wialon.remote.handlers.ResponseHandler;
import com.sdk.wialon.remote.handlers.SearchResponseHandler;

public class Demo {

	
	
	
	private Session session;
	 
	public Demo() {
		// get instance of current Session
				session=Session.getInstance();
	}
	
	
	// Login to server
	public void login(){
		// initialize Wialon session
		session.initSession(Constants.BASE_URL);
		// trying login
		// token use for https://sdk.wialon.com/playground/demo
		session.loginToken(Constants.TOKEN_APP, new ResponseHandler() {
			@Override
			public void onSuccess(String response) {
				super.onSuccess(response);
				// login succeed
				System.out.println(String.format("Logged successfully. User name is %s", session.getCurrUser().getName()));
				//call search units
				searchUnits();
			}
 
			@Override
			public void onFailure(int errorCode, Throwable throwableError) {
				super.onFailure(errorCode, throwableError);
				// login failed, print error
				System.out.println(Errors.getErrorText(errorCode));
			}
		});
	}
	
	private void searchUnits(){
		//Create new search specification
		SearchSpec searchSpec=new SearchSpec();
		//Set items type to search avl_units
		searchSpec.setItemsType(Item.ItemType.avl_unit);
		//Set property name to search
		searchSpec.setPropName("sys_name");
		//Set property value mask to search all units
		searchSpec.setPropValueMask("*");
		//Set sort type by units name
		searchSpec.setSortType("sys_name");
		//Send search by created search specification with items base data flag and from 0 to maximum number
		session.searchItems(searchSpec, 1, Item.dataFlag.base.getValue(), 0, Integer.MAX_VALUE, new SearchResponseHandler() {
			@Override
			public void onSuccessSearch(Item... items) {
				super.onSuccessSearch(items);
				// Search succeed
				System.out.println("Search items is successful");
				printUnitsNames(items);
				logout();
			}
			@Override
			public void onFailure(int errorCode, Throwable throwableError) {
				super.onFailure(errorCode, throwableError);
				// search item failed, print error
				System.out.println(Errors.getErrorText(errorCode));
				logout();
			}
		});
	}

	private void printUnitsNames(Item... items){
		if (items!=null && items.length>0) {
			System.out.println(String.format("%d units found\r\nPrinting their names...", items.length));
			//Print items names
			for (Item item : items)
				System.out.println(String.format("\t%s", item.getName()));
		}
	}
	// Logout
	private void logout(){
		session.logout(new ResponseHandler() {
			@Override
			public void onSuccess(String response) {
				super.onSuccess(response);
				// logout succeed
				System.out.println("Logout successfully");
				System.exit(0);
			}

			@Override
			public void onFailure(int errorCode, Throwable throwableError) {
				super.onFailure(errorCode, throwableError);
				// logout failed, print error
				System.out.println(Errors.getErrorText(errorCode));
				System.exit(0);
			}
		});
	}
	
}
