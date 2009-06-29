/**
 * Copyright 2009, Frederic Bregier, and individual contributors by the @author
 * tags. See the COPYRIGHT.txt in the distribution for a full listing of
 * individual contributors. This is free software; you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of the License,
 * or (at your option) any later version. This software is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * software; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF site:
 * http://www.fsf.org.
 */
package goldengate.r66.core.data;

import goldengate.common.command.exception.CommandAbstractException;
import goldengate.common.file.FileInterface;

import java.util.List;

/**
 * Class that owns one transfer to be run
 * 
 * @author Frederic Bregier
 */
public class R66Transfer {
    /**
     * The command to execute
     */
    private final FtpCommandCode command;

    /**
     * The information (list) on which the command was executed
     */
    private final List<String> info;

    /**
     * The original path on which the command was executed
     */
    private String path = null;

    /**
     * Current Ftp FileInterface
     */
    private final FileInterface currentFile;

    /**
     * The status
     */
    private boolean status = false;

    /**
     * @param command
     * @param fileOrInfo
     * @param path
     */
    public R66Transfer(FtpCommandCode command, List<String> fileOrInfo,
            String path) {
        this.command = command;
        info = fileOrInfo;
        this.path = path;
        currentFile = null;
    }

    /**
     * @param command
     * @param file
     */
    public R66Transfer(FtpCommandCode command, FileInterface file) {
        this.command = command;
        currentFile = file;
        try {
            path = file.getFile();
        } catch (final CommandAbstractException e) {
        }
        info = null;
    }

    /**
     * @return the command
     */
    public FtpCommandCode getCommand() {
        return command;
    }

    /**
     * @return the file
     * @throws FtpNoFileException
     */
    public FileInterface getFtpFile() throws FtpNoFileException {
        if (currentFile == null) {
            throw new FtpNoFileException("No file associated with the transfer");
        }
        return currentFile;
    }

    /**
     * @return the Info
     */
    public List<String> getInfo() {
        return info;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @return the status
     */
    public boolean getStatus() {
        return status;
    }

    /**
     * @param status
     */
    public void setStatus(boolean status) {
        this.status = status;
    }

    /**
	 *
	 */
    @Override
    public String toString() {
        return command.name() + " " + path;
    }
}
