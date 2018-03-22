/*
 * Copyright 2014 Gurtam
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 */

package com.sdk.wialon.item.prop;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.sdk.wialon.core.Session;
import com.sdk.wialon.item.Item;
import com.sdk.wialon.remote.RemoteHttpClient;
import com.sdk.wialon.remote.handlers.ResponseHandler;

import java.util.Collection;
import java.util.Map;

public class ItemProperties {
	private Map<String, String> data;
	private String ajaxPath;
	private Enum event;
	protected Item item;

	private void setData(Map<String, String> data){
		this.data=data;
	}

	public ItemProperties (Map<String, String> data, String propName, Item item, Enum event, String ajaxPath) {
		this.data=data;
		this.ajaxPath=ajaxPath;
		this.event=event;
		this.item=item;
		item.registerItemPropertyHandler(propName, new Item.UpdateItemProperty() {
			@Override
			public void updateItemProperty(JsonElement propData) {
				setData((Map<String, String>)Session.getInstance().getGson().fromJson(propData, new TypeToken<Map<String, String>>(){}.getType()));
			}
		});
		item.registerItemPropertyHandler(propName+"u", new Item.UpdateItemProperty() {
			@Override
			public void updateItemProperty(JsonElement data) {
				modifyProperties(data.toString(), null, false);
			}
		});
	}

	/**
	 * Return item properties json
	 * @return collection of item properties
	 */
	public Collection<String> getProperties(){
		return data==null ? null : data.values();
	}
	/**
	 * Return property item json
	 * @param id item index
	 */
	public String getProperty(long id){
		return data==null ? null : data.get(String.valueOf(id));
	}

	/**
	 * Handle result of update property item
	 * @param callback callback function
	 * @param result json in form [Integer(id), Object or null]
	 * @param skipFlag skip
	 */
	private void modifyProperties(String result, ResponseHandler callback, boolean skipFlag){
		if (result!=null) {
			JsonElement jsonResult=Session.getInstance().getJsonParser().parse(result);
			JsonArray jsonArrayResult;
			if (data!=null && jsonResult.isJsonArray() && (jsonArrayResult=jsonResult.getAsJsonArray()).size()==2) {
				String id=jsonArrayResult.get(0).toString();
				String newData=null;
				if (!jsonArrayResult.get(1).isJsonNull())
					newData=jsonArrayResult.get(1).toString();
				String oldData=data.get(id);
				if (newData != null)
					// update/create item
					data.put(id, newData);
				else if (oldData!=null &&!skipFlag)
					data.remove(id);
				// fire property update event
				if (!skipFlag && !String.valueOf(newData).equals(String.valueOf(oldData)))
					item.fireItemPropertyEvent(event, oldData, newData);
				if (callback!=null)
					callback.onSuccess(result);
				return;
			}
		}
		if (callback!=null)
			callback.onFailure(6, null);
	}

	/**
	 * Create new property item
	 * @param itemJson Json item
	 * @param callback callback function
	 */
	public void createProperty(String itemJson, ResponseHandler callback) {
		if (itemJson!=null) {
			JsonElement json=Session.getInstance().getJsonParser().parse(itemJson);
			if (json.isJsonObject()) {
				JsonObject jsonObject=json.getAsJsonObject();
				jsonObject.addProperty("itemId", item.getId());
				jsonObject.addProperty("id", 0);
				jsonObject.addProperty("callMode", "create");
				RemoteHttpClient.getInstance().remoteCall(
						ajaxPath,
						jsonObject,
						new ResponseHandler(callback) {
							@Override
							public void onSuccess(String response) {
								modifyProperties(response, getCallback(), false);
							}
						}
				);
				return;
			}
		}
		callback.onFailure(4, null);
	}

	/**
	 * Update property item
	 * @param itemJson Json item
	 * @param callback callback function
	 */
	public void updateProperty(String itemJson, String callMode, ResponseHandler callback) {
		if (itemJson!=null) {
			JsonElement json=Session.getInstance().getJsonParser().parse(itemJson);
			if (json.isJsonObject()) {
				JsonObject jsonObject=json.getAsJsonObject();
				jsonObject.addProperty("itemId", item.getId());
				jsonObject.addProperty("callMode", callMode==null ? "update" : callMode);
				RemoteHttpClient.getInstance().remoteCall(
						ajaxPath,
						jsonObject,
						new ResponseHandler(callback) {
							@Override
							public void onSuccess(String response) {
								modifyProperties(response, getCallback(), false);
							}
						}
				);
				return;
			}
		}
		callback.onFailure(4, null);
	}

	public void updateProperty(String itemJson, ResponseHandler callback) {
		updateProperty(itemJson, null, callback);
	}
	/**
	 * Delete property item
	 * @param id item index
	 * @param callback callback function
	 * @param skipFlag skip update
	 */
	public void deleteProperty(long id, ResponseHandler callback, final boolean skipFlag) {
		RemoteHttpClient.getInstance().remoteCall(
				ajaxPath,
				"{\"itemId\":"+item.getId()+",\"id\":"+id+",\"callMode\":\"delete\"}",
				new ResponseHandler(callback) {
					@Override
					public void onSuccess(String response) {
						modifyProperties(response, getCallback(), skipFlag);
					}
				}
		);
	}
}
