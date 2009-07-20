/**
 * Copyright 2009, Frederic Bregier, and individual contributors
 * by the @author tags. See the COPYRIGHT.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package openr66.database.data;

import java.sql.Types;

import openr66.authentication.R66SimpleAuth;
import openr66.database.DbConstant;
import openr66.database.R66DbPreparedStatement;
import openr66.database.exception.OpenR66DatabaseException;
import openr66.database.exception.OpenR66DatabaseNoDataException;
import openr66.database.exception.OpenR66DatabaseSqlError;

/**
 * @author Frederic Bregier
 *
 */
public class R66DbAuthent extends AbstractDbData  {
    public static enum Columns {
        hostkey, adminrole,
        updatedinfo,
        hostid
    }
    public static int [] dbTypes = {
        Types.VARBINARY, Types.BIT,
        Types.INTEGER, Types.VARCHAR
    };
    public static String table = " hosts ";


    private String hostid;

    private byte[] hostkey;

    private boolean adminrole;

    private int updatedInfo;

    private boolean isSaved = false;

    // ALL TABLE SHOULD IMPLEMENT THIS
    private DbValue primaryKey = new DbValue(hostid, Columns.hostid.name());
    private DbValue[] otherFields = {
      new DbValue(hostkey, Columns.hostkey.name()),
      new DbValue(adminrole, Columns.adminrole.name()),
      new DbValue(updatedInfo, Columns.updatedinfo.name())
    };
    private DbValue[] allFields = {
      otherFields[0], otherFields[1], otherFields[2], primaryKey
    };
    private static final String selectAllFields =
        Columns.hostkey.name()+","+Columns.adminrole.name()+
        ","+Columns.updatedinfo.name()+
        ","+Columns.hostid.name();
    private static final String updateAllFields =
        Columns.hostkey.name()+"=?,"+Columns.adminrole.name()+
        "=?,"+Columns.updatedinfo.name()+"=?";
    private static final String insertAllValues = " (?,?,?,?) ";

    @Override
    protected void setToArray() {
        allFields[Columns.hostid.ordinal()].setValue(this.hostid);
        allFields[Columns.hostkey.ordinal()].setValue(this.hostkey);
        allFields[Columns.adminrole.ordinal()].setValue(this.adminrole);
        allFields[Columns.updatedinfo.ordinal()].setValue(this.updatedInfo);
    }
    @Override
    protected void setFromArray() throws OpenR66DatabaseSqlError {
        this.hostid = (String) allFields[Columns.hostid.ordinal()].getValue();
        this.hostkey = (byte[]) allFields[Columns.hostkey.ordinal()].getValue();
        this.adminrole = (Boolean) allFields[Columns.adminrole.ordinal()].getValue();
        this.updatedInfo = (Integer) allFields[Columns.updatedinfo.ordinal()].getValue();
    }


    /**
     * @param hostid
     * @param hostkey
     * @param adminrole
     * @param updatedInfo
     */
    public R66DbAuthent(String hostid, byte[] hostkey, boolean adminrole, int updatedInfo) {
        this.hostid = hostid;
        this.hostkey = hostkey;
        this.adminrole = adminrole;
        this.updatedInfo = updatedInfo;
        this.setToArray();
        this.isSaved = false;
    }


    /**
     * @param hostid
     * @throws OpenR66DatabaseException
     */
    public R66DbAuthent(String hostid) throws OpenR66DatabaseException {
        this.hostid = hostid;
        // load from DB
        select();
    }

    /* (non-Javadoc)
     * @see openr66.database.data.AbstractDbData#delete()
     */
    @Override
    public void delete() throws OpenR66DatabaseException {
        R66DbPreparedStatement preparedStatement =
            new R66DbPreparedStatement(DbConstant.admin.session);
        try {
            preparedStatement.createPrepareStatement("DELETE FROM "+
                    table+" WHERE "+Columns.hostid.name()+" = ?");
            primaryKey.setValue(hostid);
            this.setValue(preparedStatement, primaryKey);
            int count = preparedStatement.executeUpdate();
            preparedStatement.realClose();
            if (count <= 0) {
                throw new OpenR66DatabaseNoDataException("No row found");
            }
            this.isSaved = false;
        } finally {
            preparedStatement.realClose();
        }
    }

    /* (non-Javadoc)
     * @see openr66.database.data.AbstractDbData#insert()
     */
    @Override
    public void insert() throws OpenR66DatabaseException {
        if (this.isSaved) {
            return;
        }
        R66DbPreparedStatement preparedStatement =
            new R66DbPreparedStatement(DbConstant.admin.session);
        try {
            preparedStatement.createPrepareStatement("INSERT INTO "+table+" ("+selectAllFields+
                    ") VALUES "+insertAllValues);
            this.setValues(preparedStatement, allFields);
            int count = preparedStatement.executeUpdate();
            preparedStatement.realClose();
            if (count <= 0) {
                throw new OpenR66DatabaseNoDataException("No row found");
            }
            this.isSaved = true;
        } finally {
            preparedStatement.realClose();
        }
    }

    /* (non-Javadoc)
     * @see openr66.database.data.AbstractDbData#select()
     */
    @Override
    public void select() throws OpenR66DatabaseException {
        R66DbPreparedStatement preparedStatement =
            new R66DbPreparedStatement(DbConstant.admin.session);
        try {
            preparedStatement.createPrepareStatement("SELECT "+selectAllFields+" FROM "+
                    table+" WHERE "+Columns.hostid.name()+" = ?");
            primaryKey.setValue(hostid);
            this.setValue(preparedStatement, primaryKey);
            preparedStatement.executeQuery();
            if (preparedStatement.getNext()) {
                this.getValues(preparedStatement, allFields);
                this.setFromArray();
                this.isSaved = true;
            } else {
                throw new OpenR66DatabaseNoDataException("No row found");
            }
        } finally {
            preparedStatement.realClose();
        }
    }

    /* (non-Javadoc)
     * @see openr66.database.data.AbstractDbData#update()
     */
    @Override
    public void update() throws OpenR66DatabaseException {
        if (this.isSaved) {
            return;
        }
        R66DbPreparedStatement preparedStatement =
            new R66DbPreparedStatement(DbConstant.admin.session);
        try {
            preparedStatement.createPrepareStatement("UPDATE "+table+" SET "+updateAllFields+
                    " WHERE "+Columns.hostid.name()+" = ?");
            this.setValues(preparedStatement, allFields);
            int count = preparedStatement.executeUpdate();
            preparedStatement.realClose();
            if (count <= 0) {
                throw new OpenR66DatabaseNoDataException("No row found");
            }
            this.isSaved = true;
        } finally {
            preparedStatement.realClose();
        }
    }

    /* (non-Javadoc)
     * @see openr66.database.data.AbstractDbData#changeUpdatedInfo(int)
     */
    @Override
    public void changeUpdatedInfo(int status) {
        if (this.updatedInfo != status) {
            this.updatedInfo = status;
            allFields[Columns.updatedinfo.ordinal()].setValue(this.updatedInfo);
            this.isSaved = false;
        }
    }

    public R66SimpleAuth getR66SimpleAuth() {
        R66SimpleAuth auth = new R66SimpleAuth(this.hostid, this.hostkey);
        auth.setAdmin(this.adminrole);
        return auth;
    }
}