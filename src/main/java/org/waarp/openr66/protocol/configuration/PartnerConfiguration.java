/**
   This file is part of Waarp Project.

   Copyright 2009, Frederic Bregier, and individual contributors by the @author
   tags. See the COPYRIGHT.txt in the distribution for a full listing of
   individual contributors.

   All Waarp Project is free software: you can redistribute it and/or 
   modify it under the terms of the GNU General Public License as published 
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Waarp is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Waarp .  If not, see <http://www.gnu.org/licenses/>.
 */
package org.waarp.openr66.protocol.configuration;

import org.waarp.common.digest.FilesystemBasedDigest.DigestAlgo;
import org.waarp.common.json.JsonHandler;
import org.waarp.common.logging.WaarpInternalLogger;
import org.waarp.common.logging.WaarpInternalLoggerFactory;
import org.waarp.openr66.protocol.utils.R66Versions;
import org.waarp.openr66.protocol.utils.Version;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Partner Configuration
 * @author "Frederic Bregier"
 *
 */
public class PartnerConfiguration {
	/**
	 * Internal Logger
	 */
	private static final WaarpInternalLogger logger = WaarpInternalLoggerFactory
			.getLogger(PartnerConfiguration.class);

	/**
	 * Uses as separator in field
	 */
	public static final String BAR_JSON_FIELD = "{";
	/**
	 * Uses as separator in field
	 */
	public static final String BAR_SEPARATOR_FIELD = ";";
	/**
	 * Uses as separator in field
	 */
	public static final String BLANK_SEPARATOR_FIELD = " ";
	/**
	 * Uses as separator in field
	 */
	public static String SEPARATOR_FIELD = BAR_SEPARATOR_FIELD;

	/**
	 * JSON Fields
	 *
	 */
	public static enum FIELDS {
		HOSTID("nohostid"), VERSION(R66Versions.V2_4_12.getVersion()), 
		DIGESTALGO(DigestAlgo.MD5.name), FILESIZE(false), FINALHASH(false), 
		PROXIFIED(false), SEPARATOR(BLANK_SEPARATOR_FIELD);
		
		String name;
		Object defaultValue;
		private FIELDS(Object def) {
			this.name = name();
			this.defaultValue = def;
		}
	}
	private String id;
	private ObjectNode root = JsonHandler.createObjectNode();
	private boolean useJson = false;
	
	/**
	 * Constructor for an external HostId
	 * @param id
	 * @param json mainly the version information
	 */
	public PartnerConfiguration(String id, String json) {
		this.id = id;
		JsonHandler.setValue(root, FIELDS.HOSTID, id);
		int pos = json.lastIndexOf('{');
		String version = null;
		if (pos > 1) {
			version = json.substring(0, pos-1);
		} else {
			version = json;
		}
		JsonHandler.setValue(root, FIELDS.VERSION, version);
		if (isVersion2GEQVersion1(R66Versions.V2_4_12.getVersion(), version)) {
			JsonHandler.setValue(root, FIELDS.FILESIZE, true);
			JsonHandler.setValue(root, FIELDS.FINALHASH, true);
		} else {
			JsonHandler.setValue(root, FIELDS.FILESIZE, (Boolean) FIELDS.FILESIZE.defaultValue);
			JsonHandler.setValue(root, FIELDS.FINALHASH, (Boolean) FIELDS.FINALHASH.defaultValue);
		}
		JsonHandler.setValue(root, FIELDS.DIGESTALGO, Configuration.configuration.digest.name);
		JsonHandler.setValue(root, FIELDS.PROXIFIED, (Boolean) FIELDS.PROXIFIED.defaultValue);
		String sep = SEPARATOR_FIELD;
		if (! isVersion2GEQVersion1(R66Versions.V2_4_13.getVersion(), version)) {
			sep = BLANK_SEPARATOR_FIELD;
		}
		if (isVersion2GEQVersion1(R66Versions.V2_4_17.getVersion(), version)) {
			logger.debug("UseJson for "+id+":"+json);
			useJson = true;
		} else {
			logger.debug("NOT UseJson for "+id+":"+json);
			useJson = false;
		}
		JsonHandler.setValue(root, FIELDS.SEPARATOR, sep);
		
		if (json != null && pos > 1) {
			String realjson = json.substring(pos);
			ObjectNode info = JsonHandler.getFromString(realjson);
			if (info != null) {
				root.putAll(info);
			}
		}
		logger.debug("Info HostId: "+root.toString());
	}
	
	/**
	 * Self constructor
	 * @param id 
	 */
	public PartnerConfiguration(String id) {
		this.id = id;
		JsonHandler.setValue(root, FIELDS.HOSTID, id);
		JsonHandler.setValue(root, FIELDS.VERSION, Version.ID);
		JsonHandler.setValue(root, FIELDS.FILESIZE, true);
		JsonHandler.setValue(root, FIELDS.FINALHASH, Configuration.configuration.globalDigest);
		JsonHandler.setValue(root, FIELDS.DIGESTALGO, Configuration.configuration.digest.name);
		JsonHandler.setValue(root, FIELDS.PROXIFIED, Configuration.configuration.isHostProxyfied);
		JsonHandler.setValue(root, FIELDS.SEPARATOR, SEPARATOR_FIELD);
		useJson = true;
		logger.debug("Info HostId: "+root.toString());
	}
	
	/**
	 * 
	 * @return the associated HostId
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * 
	 * @return the version for this Host
	 */
	public String getVersion() {
		return root.path(FIELDS.VERSION.name).asText();
	}
	/**
	 * 
	 * @return True if this Host returns FileSize
	 */
	public boolean useFileSize() {
		return root.path(FIELDS.FILESIZE.name).asBoolean((Boolean) FIELDS.FILESIZE.defaultValue);
	}
	/**
	 * 
	 * @return True if this Host returns a final hash
	 */
	public boolean useFinalHash() {
		return root.path(FIELDS.FINALHASH.name).asBoolean((Boolean) FIELDS.FINALHASH.defaultValue);
	}
	/**
	 * 
	 * @return True if this Host returns Digest Algo used
	 */
	public DigestAlgo getDigestAlgo() {
		String algo = root.path(FIELDS.DIGESTALGO.name).asText();
		return getDigestAlgo(algo);
	}
	/**
	 * 
	 * @return True if this Host is proxified
	 */
	public boolean isProxified() {
		return root.path(FIELDS.PROXIFIED.name).asBoolean((Boolean) FIELDS.PROXIFIED.defaultValue);
	}
	/**
	 * 
	 * @return the separator for this Host
	 */
	public String getSeperator() {
		return root.path(FIELDS.SEPARATOR.name).asText();
	}
	
	/**
	 * @return the useJson
	 */
	public boolean useJson() {
		return useJson;
	}
	
	/**
	 * 
	 * @return the String representation as version.json
	 */
	public String toString() {
		return getVersion()+"."+JsonHandler.writeAsString(root);
	}
	
	public final static DigestAlgo getDigestAlgo(String algo) {
		for (DigestAlgo alg : DigestAlgo.values()) {
			if (alg.name.equals(algo)) {
				return alg;
			}
		}
		try {
			return DigestAlgo.valueOf(algo);
		} catch (IllegalArgumentException e) {
		}
		return Configuration.configuration.digest;
	}
	/**
	 * 
	 * @param remoteHost
	 * @return the separator to be used
	 */
	public final static String getSeparator(String remoteHost) {
		logger.debug("Versions: search: "+remoteHost+ " in {}", Configuration.configuration.versions);
		PartnerConfiguration partner = Configuration.configuration.versions.get(remoteHost);
		if (partner != null) {
			return partner.getSeperator();
		}
		return BLANK_SEPARATOR_FIELD;
	}
	/**
	 * Compare 2 versions
	 * @param version1
	 * @param version2
	 * @return True if version2 >= version1
	 */
	public final static boolean isVersion2GEQVersion1(String version1, String version2) {
		if (version1 == null || version2 == null) {
			return false;
		}
		int major1 = 0;
		int rank1 = 0;
		int subversion1 = 0;
		String [] vals = version1.split("\\.");
		major1 = Integer.parseInt(vals[0]);
		rank1 = Integer.parseInt(vals[1]);
		subversion1 = Integer.parseInt(vals[2]);
		int major2 = 0;
		int rank2 = 0;
		int subversion2 = 0;
		vals = version2.split("\\.");
		major2 = Integer.parseInt(vals[0]);
		rank2 = Integer.parseInt(vals[1]);
		subversion2 = Integer.parseInt(vals[2]);
		logger.debug("1: "+major1+":"+rank1+":"+subversion1+" <=? "+major2+":"+rank2+":"+subversion2+ " = "+
				(major1 < major2 || (major1 == major2 && (rank1 < rank2 || (rank1 == rank2 && subversion1 <= subversion2)))));
		return (major1 < major2 || (major1 == major2 && (rank1 < rank2 || (rank1 == rank2 && subversion1 <= subversion2))));
	}

	/**
	 * 
	 * @param host
	 * @return True if this host is referenced as using Json
	 */
	public final static boolean useJson(String host) {
		logger.debug("UseJson host: '"+host+"':"+(Configuration.configuration.versions.containsKey(host) ?  
			Configuration.configuration.versions.get(host).useJson(): "no:"+Configuration.configuration.versions.keySet()) );
		return (Configuration.configuration.versions.containsKey(host) && 
				Configuration.configuration.versions.get(host).useJson());
	}
}
