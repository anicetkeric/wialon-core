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

import com.sdk.wialon.core.Session;
import com.sdk.wialon.item.Item;
import com.sdk.wialon.remote.RemoteHttpClient;
import com.sdk.wialon.remote.handlers.ResponseHandler;
import com.sdk.wialon.render.Renderer;

import java.util.Map;

public class Report extends ItemPropertiesData {

	public Report(Map<String, String> data, String propName, Item item, String ajaxPath, String extAjaxPath) {
		super(data, propName, item, events.updateReport, ajaxPath, extAjaxPath);
	}

	public static class ReportInterval {
		private long from;
		private long to;
		private int flags;

		public ReportInterval(long from, long to, int flags) {
			this.from = from;
			this.to = to;
			this.flags = flags;
		}
	}

	/**
	 * Generate report and load it into session, require ACL wialon.core.Item.accessFlag.execReports to report object.
	 * May load into renderer special layer to turn on/off report visibility. Layer presence in renderer is managed automatically.
	 *
	 * @param reportId          report, compact or full
	 * @param reportObjectId    report object ID
	 * @param reportObjectSecId secondary report object ID, e.g. driver id for driver reports
	 * @param interval          report interval specification
	 * @param callback          callback that will receive information about new layer addition
	 */
	public void execReport(long reportId, long reportObjectId, long reportObjectSecId, ReportInterval interval, ResponseHandler callback) {
		//reportTemplate: reportTemplate,
		RemoteHttpClient.getInstance().remoteCall(
				"report/exec_report",
				"{\"reportResourceId\":" + item.getId() + ",\"reportTemplateId\":" + reportId + ",\"reportObjectId\":" + reportObjectId + "," +
						"\"reportObjectSecId\":" + reportObjectSecId + ",\"interval\":" + Session.getInstance().getGson().toJson(interval) + "}",
				new ResponseHandler(callback) {
					@Override
					public void onSuccess(String response) {
						createReportResult(getCallback(), response);
					}
				}
		);
	}

	/**
	 * Cleanup report execution result.
	 * May load into renderer special layer to turn on/off report visibility. Layer presence in renderer is managed automatically.
	 *
	 * @param callback {Function} callback that will receive information about new layer addition: callback(code, reportResult), zero code is success
	 */
	public void cleanupResult(ResponseHandler callback) {
		RemoteHttpClient.getInstance().remoteCall(
				"report/cleanup_result",
				"{}",
				new ResponseHandler(callback) {
					@Override
					public void onSuccess(String response) {
						cleanupReportResult(getCallback(), response);
					}
				}
		);
	}

	/**
	 * Handle result of report execution
	 *
	 * @param result   report result information
	 * @param callback user-defined callback
	 */
	private void createReportResult(ResponseHandler callback, String result) {
		if (result == null) {
			// error
			callback.onFailure(6, null);
			return;
		}
		callback.onSuccess(result);
		//TODO: update session render
	}

	/**
	 * Handle result cleanup
	 *
	 * @param callback user-defined callback
	 */
	private void cleanupReportResult(ResponseHandler callback, String result) {
		Renderer renderer = Session.getInstance().getRenderer();
		// update renderer
		if (renderer != null)
			renderer.hashCode();//TODO: update session render
		// pass code to callback if available
		callback.onSuccess(result);
	}

	public enum events {
		/**
		 * report property has updated
		 */
		updateReport
	}

	/**
	 * Report interval flags constants
	 */
	public enum intervalFlag {
		/**
		 * Default interval time - absolute specification of time_from-time_to
		 */
		absolute(0x00),
		/**
		 * Bit, specifying that for report calculation(time_from) will be used current system time
		 */
		useCurrentTime(0x01),
		/**
		 * Use previous hour against time_from in user's timezone
		 */
		prevHour(0x40),
		/**
		 * Use previous day against time_from in user's timezone
		 */
		prevDay(0x02),
		/**
		 * Use previous week against time_from in user's timezone
		 */
		prevWeek(0x04),
		/**
		 * Use previous month against time_from in user's timezone
		 */
		prevMonth(0x08),
		/**
		 * Use previous year against time_from in user's timezone
		 */
		prevYear(0x10),
		/**
		 * Specifying that for report calculation(time_from) will be used current system time + prev (day, week, month, year)
		 */
		currTimeAndPrev(0x20);
		/**
		 * Flag value
		 */
		private long value;

		private intervalFlag(long value) {
			this.value = value;
		}

		public long getValue() {
			return value;
		}
	}

	/**
	 * Report table flags constants, TODO: fill later...
	 */
	public enum tableFlag {
	}

	/**
	 * Report column flags constants, TODO: fill later...
	 */
	public enum columnFlag {
	}
}
