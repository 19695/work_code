package org.colm.code.sftp;

import com.jcraft.jsch.SftpException;
import org.colm.code.sftp.constant.ModeEnum;
import org.colm.code.sftp.tool.Assistant;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.colm.code.AssertUtil.assertNotNull;

public class SftpMultiServerUtil {

    private Assistant assistant;

    /**
     * 单文件上传
     * @param localFile
     * @param clientId
     */
    public static void upload(File localFile, String clientId) throws Exception {
        upload(Arrays.asList(localFile), clientId);
    }

    /**
     * 多文件上传
     * @param localFileList
     * @param clientId
     */
    public static void upload(List<File> localFileList, String clientId) throws Exception {
        assertNotNull(localFileList, "请指定需要上传的文件");
        SftpMultiServerUtil util = new SftpMultiServerUtil();
        try {
            util.getConnect(clientId);
            for (File file : localFileList) {
                util.upload(file.getName(), file);
            }
        } finally {
            util.disconnect();
        }
    }

    /**
     * 单文件下载
     * @param remoteRelativePath
     * @param localAbsolutePath
     * @param clientId
     * @return
     */
    public static File download(String remoteRelativePath, String localAbsolutePath, String clientId) throws Exception {
        assertNotNull(remoteRelativePath, "请指定要下载的文件的路径");
        assertNotNull(localAbsolutePath, "请指定下载后的文件路径");
        SftpMultiServerUtil util = new SftpMultiServerUtil();
        try {
            util.getConnect(clientId);
            return util.download(remoteRelativePath, localAbsolutePath);
        } finally {
            util.disconnect();
        }
    }

    /**
     * 获取连接
     * @param clientId
     * @throws Exception
     */
    private void getConnect(String clientId) throws Exception {
        if (assistant == null) {
            assistant = Assistant.of(clientId);
        }
    }

    /**
     * 归还连接
     * @throws Exception
     */
    private void disconnect() throws Exception {
        if (assistant != null) {
            assistant.returnConnect();
        }
    }

    private void upload(String remoteRelativePath, File localFile) throws SftpException, IOException {
        String remoteAbsolutePath = getRemoteAbsolutePath(remoteRelativePath);
        assistant.getClient().upload(remoteAbsolutePath, localFile);
    }

    /**
     * 预留出来的，可以指定文件上传模式的
     * @param remoteRelativePath
     * @param localFile
     * @param mode
     * @throws SftpException
     * @throws IOException
     */
    private void upload(String remoteRelativePath, File localFile, ModeEnum mode) throws SftpException, IOException {
        String remoteAbsolutePath = getRemoteAbsolutePath(remoteRelativePath);
        assistant.getClient().upload(remoteAbsolutePath, localFile, mode);
    }

    private File download(String remoteRelativePath, String localAbsolutePath) throws SftpException, IOException {
        String remoteAbsolutePath = getRemoteAbsolutePath(remoteRelativePath);
        return assistant.getClient().download(remoteAbsolutePath, localAbsolutePath);
    }

    /**
     * 预留出来的，可以指定文件下载模式的
     * @param remoteRelativePath
     * @param localAbsolutePath
     * @param mode
     * @return
     * @throws SftpException
     * @throws IOException
     */
    private File download(String remoteRelativePath, String localAbsolutePath, ModeEnum mode) throws SftpException, IOException {
        String remoteAbsolutePath = getRemoteAbsolutePath(remoteRelativePath);
        return assistant.getClient().download(remoteAbsolutePath, localAbsolutePath, mode);
    }

    private String getRemoteAbsolutePath(String remoteRelativePath) throws SftpException {
        String defaultPath = assistant.getClient().getDefaultPath();
        return new File(defaultPath, remoteRelativePath).getAbsolutePath();
    }

}
