package org.colm.code.sftp.pool;

import com.jcraft.jsch.*;
import org.colm.code.sftp.constant.ChannelEnum;
import org.colm.code.sftp.constant.ModeEnum;

import java.io.*;
import java.util.Properties;
import java.util.Vector;

public class SftpClient {

    private final Session session;
    private ChannelSftp channelSftp;
    private final String defaultPath;

    public SftpClient(SftpClientConfigure configure) throws JSchException {
        this.defaultPath = configure.getHostPath();
        JSch jSch = new JSch();
        session = jSch.getSession(configure.getUsername(), configure.getHost(), configure.getPort());
        session.setPassword(configure.getPassword());
        Properties properties = new Properties();
        properties.put("StrictHostKeyChecking", "no");
        session.setConfig(properties);
        session.setTimeout(configure.getConnectTimeout());
        session.connect();
    }
    
    protected boolean connect() throws JSchException {
        channelSftp = (ChannelSftp) session.openChannel(ChannelEnum.SFTP.getChannel());
        channelSftp.connect();
        return isConnected();
    }

    protected boolean isConnected() {
        return isSessionConnected() ? isChannelConnected() : false;
    }

    protected boolean isSessionConnected() {
        return session == null ? false : session.isConnected();
    }

    protected boolean isChannelConnected() {
        return channelSftp == null ? false : session.isConnected();
    }

    protected void disconnect() {
        channelDisConnect();
        sessionDisConnect();
    }

    private void channelDisConnect() {
        if (channelSftp != null)
            channelSftp.disconnect();
    }

    private void sessionDisConnect() {
        if (session != null)
            session.disconnect();
    }

    /**
     * 指定文件下载模式进行下载
     * @param sftpAbsolutePath
     * @param localAbsolutePath
     * @param mode
     * @return
     * @throws IOException
     * @throws SftpException
     */
    public File download(String sftpAbsolutePath, String localAbsolutePath, ModeEnum mode) throws IOException, SftpException {
        File localFile = getFile(localAbsolutePath);
        try (OutputStream outputStream = new FileOutputStream(localFile)) {
            channelSftp.get(sftpAbsolutePath, outputStream, null, mode.getValue(), 0L);
            return localFile;
        }
    }

    /**
     * 覆盖模式下载
     * @param sftpAbsolutePath
     * @param localAbsolutePath
     * @return
     * @throws IOException
     * @throws SftpException
     */
    public File download(String sftpAbsolutePath, String localAbsolutePath) throws IOException, SftpException {
        return download(sftpAbsolutePath, localAbsolutePath, ModeEnum.OVERWRITE);
    }

    /**
     * 指定文件上传模式进行上传
     * @param sftpAbsolutePath
     * @param localFile
     * @param mode
     * @throws IOException
     * @throws SftpException
     */
    public void upload(String sftpAbsolutePath, File localFile, ModeEnum mode) throws IOException, SftpException {
        try (OutputStream outputStream = channelSftp.put(sftpAbsolutePath, mode.getValue());
             InputStream inputStream = new FileInputStream(localFile)
        ) {
            byte[] buf = new byte[1024];
            int length;
            while ((length = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, length);
            }
            outputStream.flush();
        }
    }

    /**
     * 覆盖模式上传
     * @param sftpAbsolutePath
     * @param localFile
     * @throws IOException
     * @throws SftpException
     */
    public void upload(String sftpAbsolutePath, File localFile) throws IOException, SftpException {
        upload(sftpAbsolutePath, localFile, ModeEnum.OVERWRITE);
    }

    /**
     * 创建路径
     * @param path
     * @throws SftpException
     */
    public void createDirectory(String path) throws SftpException {
        String[] dirs = path.split(File.separator);
        String currentDir;
        if (path.startsWith(File.pathSeparator)) {
            currentDir = File.pathSeparator;
        } else {
            currentDir = "";
        }
        for (String dir : dirs) {
            if ("".equals(dir)) {
                continue;
            }
            currentDir += dir + File.separator;
            if (!isDirectory(currentDir)) {
                channelSftp.mkdir(currentDir);
            }
        }
    }

    /**
     * ls
     * @param path
     * @return
     */
    public Vector<ChannelSftp.LsEntry> listLsEntry(String path) {
        if (!isDirectory(path)) {
            return null;
        }
        try {
            return channelSftp.ls(path);
        } catch (SftpException e) {
            return null;
        }
    }

    /**
     * getHome
     * @return
     * @throws SftpException
     */
    public String getHome() throws SftpException {
        if (channelSftp != null) {
            return channelSftp.getHome();
        }
        return null;
    }

    /**
     * 如果没设置默认路径，就返回家路径
     * @return
     * @throws SftpException
     */
    public String getDefaultPath() throws SftpException {
        if (defaultPath == null || "".equals(defaultPath)) {
            return defaultPath;
        }
        return channelSftp.getHome();
    }

    private boolean isDirectory(String path) {
        try {
            channelSftp.cd(defaultPath);
            channelSftp.cd(path);
            channelSftp.cd(defaultPath);
            return true;
        } catch (SftpException e) {
            return false;
        }
    }

    private File getFile(String localAbsolutePath) throws IOException {
        File localFile = new File(localAbsolutePath);
        if (!localFile.exists()) {
            localFile.createNewFile();
        }
        return localFile;
    }

}
