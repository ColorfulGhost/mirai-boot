package moe.iacg.miraiboot.uploader;


import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;


import java.io.*;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Redemption on 2/24/2016.
 */
@Component
@Slf4j
public class OneDriveUploader {
    private boolean errorOccurred;
    private long totalUploaded;
    private long lastUploaded;

    private String accessToken = "eyJ0eXAiOiJKV1QiLCJub25jZSI6IkN6b3h6V0g5S0FZczd0UVhFZzNuYTNKV3JWWDYzYXF6WmNLVzVwUHdJdDgiLCJhbGciOiJSUzI1NiIsIng1dCI6Im5PbzNaRHJPRFhFSzFqS1doWHNsSFJfS1hFZyIsImtpZCI6Im5PbzNaRHJPRFhFSzFqS1doWHNsSFJfS1hFZyJ9.eyJhdWQiOiIwMDAwMDAwMy0wMDAwLTAwMDAtYzAwMC0wMDAwMDAwMDAwMDAiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC81Mzg4MTM5ZS03YzM5LTQ2N2QtYjM1MC03NDQzODZiNGY0OWIvIiwiaWF0IjoxNjI1MTUxNTQxLCJuYmYiOjE2MjUxNTE1NDEsImV4cCI6MTYyNTE1NTQ0MSwiYWNjdCI6MCwiYWNyIjoiMSIsImFjcnMiOlsidXJuOnVzZXI6cmVnaXN0ZXJzZWN1cml0eWluZm8iLCJ1cm46bWljcm9zb2Z0OnJlcTEiLCJ1cm46bWljcm9zb2Z0OnJlcTIiLCJ1cm46bWljcm9zb2Z0OnJlcTMiLCJjMSIsImMyIiwiYzMiLCJjNCIsImM1IiwiYzYiLCJjNyIsImM4IiwiYzkiLCJjMTAiLCJjMTEiLCJjMTIiLCJjMTMiLCJjMTQiLCJjMTUiLCJjMTYiLCJjMTciLCJjMTgiLCJjMTkiLCJjMjAiLCJjMjEiLCJjMjIiLCJjMjMiLCJjMjQiLCJjMjUiXSwiYWlvIjoiQVVRQXUvOFRBQUFBd1J3czdOODYwK3FZMU5RUDh2eE1iRitkWVhPTzhVelhaVW5odThPbDJCeXowZloxdlZlQ05peTdKVGpoYjczMDV6QUNmODB1WHRISmJVV01jQTIxSVE9PSIsImFtciI6WyJwd2QiLCJtZmEiXSwiYXBwX2Rpc3BsYXluYW1lIjoibXktb25lZHJpdmUtY2YtaW5kZXgiLCJhcHBpZCI6IjJhZDU4ZGM4LTgxYmYtNDdmNC04N2U0LTdlOGFhMzAyNmQ0MSIsImFwcGlkYWNyIjoiMSIsImZhbWlseV9uYW1lIjoiR2hvc3QiLCJnaXZlbl9uYW1lIjoiQ29sb3JmdWwiLCJpZHR5cCI6InVzZXIiLCJpcGFkZHIiOiIxNzUuNDEuMjM2LjExMSIsIm5hbWUiOiJDb2xvcmZ1bCBHaG9zdCIsIm9pZCI6ImMzNDdjY2VmLTMyYTctNDJmNC05YmE2LTFkMDI0YmIyOWFiNyIsInBsYXRmIjoiMyIsInB1aWQiOiIxMDAzMjAwMTJFMDc4MkQ0IiwicmgiOiIwLkFYRUFuaE9JVXpsOGZVYXpVSFJEaHJUMG04aU4xU3FfZ2ZSSGgtUi1pcU1DYlVGeEFGcy4iLCJzY3AiOiJGaWxlcy5SZWFkIEZpbGVzLlJlYWQuQWxsIEZpbGVzLlJlYWRXcml0ZS5BbGwgVXNlci5SZWFkIHByb2ZpbGUgb3BlbmlkIGVtYWlsIiwic2lnbmluX3N0YXRlIjpbImttc2kiXSwic3ViIjoiYVNtOEJ6ZDREcTkycWg0bTdLU2dkQksyeEktR0ZXVnlMYU1jbFdoQzVvVSIsInRlbmFudF9yZWdpb25fc2NvcGUiOiJBUyIsInRpZCI6IjUzODgxMzllLTdjMzktNDY3ZC1iMzUwLTc0NDM4NmI0ZjQ5YiIsInVuaXF1ZV9uYW1lIjoiR2hvc3RAaWFjZy5tb2UiLCJ1cG4iOiJHaG9zdEBpYWNnLm1vZSIsInV0aSI6IlZHSUZ3UWc0WkV5VGNxb1Nhc04tQUEiLCJ2ZXIiOiIxLjAiLCJ3aWRzIjpbIjYyZTkwMzk0LTY5ZjUtNDIzNy05MTkwLTAxMjE3NzE0NWUxMCIsImI3OWZiZjRkLTNlZjktNDY4OS04MTQzLTc2YjE5NGU4NTUwOSJdLCJ4bXNfc3QiOnsic3ViIjoic2xWQmk5SjE1Nlh5OEM5aTVQR0dLMjFNWDdzb3kycmNtZGZENmxiRm5tZyJ9LCJ4bXNfdGNkdCI6MTYxODIwMDY1OX0.M7_wESB6Uhn2vIvDcYHJ5j6WVWzIvArwhp8LKKcJvIhTZAa1JLiLqptkHCh8LWTK17rrm3SmVClJlSIAkKbakzYm4W9t8OfwzH3ZyzBj28yCvKt5gAmE875tbVkLHE22DcoQm_jJ7D-5kK86_r6vZ1IpCrh333jtAfrURwD-BSCmUfYck25HjgQywAUfjZhQbCu79Lhh_itPONuu9a4SMHGC0UHDhTw-GIhskMKnvoNCsyhlzRbz6LrSUiPB91v-U0ft-On5Rxnrl6usu4dMmyXxLR962duOVsjMSs8pewo8ZlGO04SQVRjo7D0DLhQXWgJ2UyXGgkETg2R3nJ7FRg";
    private String refreshToken = "0.AXEAnhOIUzl8fUazUHRDhrT0m8iN1Sq_gfRHh-R-iqMCbUFxAFs.AgABAAAAAAD--DLA3VO7QrddgJg7WevrAgDs_wQA9P8hyIreHHNQD7YPQ-RRbLGiuYoVqX3CA2w1dNcHThJQd7Mu7YFYLmcu5fLQGmGUGhD1YeupW2kPq9zw_m__rX95IWACiJZEsWDA1L7KJu8INHz4kIGPaSjC55CZapPX9GzRrPMyd1oAvH0gu6jxuJzm66lE1nMzuJsNDxT0meo37s7R5pGvh3-L_NFcfTJlM08DIh4irQsRA6-LQOXHkK1HhR0Dk3VitXjIbPmihiblbK2o4g3-Y2guH-vLFOnhVZxOOXHZ24U8v_MUlNkiQns2-zC1sEOaPKsvqQ_y8S-j8r7T1ujcAndCeupM8SI5_fjTKB1uaLRl3uX5WpncZDR9R5bNnBJalKEDOaOcN_GtDo7gV0hwTRuFtIP_oVzDOFR3eRLmDZHPSBWRHTMO6coU02osy720JF_-ajlm7X-Rh4OEQKwvMPECZOrrYR7MedFWfr8ppLCzyi2A7zCGQZ009SS5OBayg0-JU8VGxWCedCt3JZp9W5oKmx_VVqEEceZMEpeiT1WcPpQCydRMYnDTM6IMBcj7qhQ9oHhGsyXqYHfd2bmjjLS5vuSLiCx3Ni_mXguxxIN_3jmjyV_JzkyRdr2la_wDcXYgEwzhLwxswpNu4Bsxe0rGEpLL_Rd6GC3lmVrA8W-Dcni2hODnTEvnduOWaJzxzW83tv93aqyZX6o-pBG7PHu3KkMluyYoogHeWE9T9KERNl6_VUxFlrA8tVwV-J1CFDOkTODaDOhJx2pUEr5lbaigQ9X-ZEm7DyK9WrVM8WKaa7Rl8H_JkliC9nqNvSiEAHjBWOrzMN9P2hSRAU4q_TVL-UdZfmkyNLr8kMuyPOTeX9aOYISwp-wf0NzPNOyhsM7Man12OYsO4CIcIqBbBA0qwzhC4dz31cQn21BjuBGpDNuzs1Iw47yFotI-FYqaUuufR_8C6u5T4oVQHgt8mfuAPAPGMYn2qjOC2hIBYyI2zYFBcLMnRa3OirFYZBekjgN1hChzfg4MoCsDmyzMDVX4yFubGxiwSmh8Cg2m9MQ2FzPS-xOWAeTlv5itgnYFVbLSm1VDEsj0IxqgCtRld1Be38yYUjG2831xct_xF99Y1pHZXcg";


    private static int keepCount = 1;
    private static String destination ="/";
    /**
     * Global instance of the HTTP client
     */
    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(3, TimeUnit.MINUTES)
            .readTimeout(3, TimeUnit.MINUTES)
            .build();
    private static final MediaType zipMediaType = MediaType.parse("application/zip; charset=utf-8");
    private static final MediaType jsonMediaType = MediaType.parse("application/json; charset=utf-8");

    /**
     * Size of the file chunks to upload to OneDrive
     */
    private static final int CHUNK_SIZE = 5 * 1024 * 1024;

    /**
     * File upload buffer
     */
    private RandomAccessFile raf;

    /**
     * OneDrive API credentials
     */
    private static final String CLIENT_ID = "2ad58dc8-81bf-47f4-87e4-7e8aa3026d41";


    /**
     * Creates an instance of the {@code OneDriveUploader} object
     */
    public OneDriveUploader() {
        try {
            setRefreshTokenFromStoredValue();
            retrieveNewAccessToken();
            setRanges(new String[0]);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            setErrorOccurred(true);
        }
    }

    /**
     * Sets the authenticated user's stored OneDrive refresh token from the stored value
     *
     * @throws Exception
     */
    private void setRefreshTokenFromStoredValue() throws Exception {

        String readRefreshToken = refreshToken;

        if (readRefreshToken != null && !readRefreshToken.isEmpty()) {
            setRefreshToken(readRefreshToken);
        } else {
            setRefreshToken("");
        }
    }

    /**
     * Gets a new OneDrive access token for the authenticated user
     */
    private void retrieveNewAccessToken() throws Exception {
        RequestBody requestBody = new FormBody.Builder()
                .add("client_id", CLIENT_ID)
                .add("scope", "offline_access Files.ReadWrite")
                .add("refresh_token", returnRefreshToken())
                .add("grant_type", "refresh_token")
                .add("redirect_uri", "https://login.microsoftonline.com/common/oauth2/nativeclient")
                .build();

        Request request = new Request.Builder()
                .url("https://login.microsoftonline.com/common/oauth2/v2.0/token")
                .post(requestBody)
                .build();

        Response response = httpClient.newCall(request).execute();
        JSONObject parsedResponse = new JSONObject(response.body().string());
        response.close();

        setAccessToken(accessToken);
    }

    /**
     * Tests the OneDrive account by uploading a small file
     *
     * @param testFile the file to upload during the test
     */
    public void test(java.io.File testFile) {
        try {
            String destination = getDestination();

            Request request = new Request.Builder()
                    .addHeader("Authorization", "Bearer " + returnAccessToken())
                    .url("https://graph.microsoft.com/v1.0/me/drive/root:/" + destination + "/" + testFile.getName() + ":/content")
                    .put(RequestBody.create(testFile, MediaType.parse("plain/txt")))
                    .build();

            Response response = httpClient.newCall(request).execute();
            int statusCode = response.code();
            response.close();

            if (statusCode != 201) {
                setErrorOccurred(true);
            }

            TimeUnit.SECONDS.sleep(5);

            request = new Request.Builder()
                    .addHeader("Authorization", "Bearer " + returnAccessToken())
                    .url("https://graph.microsoft.com/v1.0/me/drive/root:/" + destination + "/" + testFile.getName() + ":/")
                    .delete()
                    .build();

            response = httpClient.newCall(request).execute();
            statusCode = response.code();
            response.close();

            if (statusCode != 204) {
                setErrorOccurred(true);
            }
        } catch (UnknownHostException exception) {
            log.error("Failed to upload test file to OneDrive, check your network connection", "drivebackup.linkAccounts", true);
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            setErrorOccurred(true);
        }
    }

    public static String getDestination() {
        if (destination.charAt(0) == java.io.File.separatorChar) {
            return destination.substring(1);
        } else {
            return destination;
        }
    }

    /**
     * Uploads the specified file to the authenticated user's OneDrive inside a folder for the specified file type
     *
     * @param file the file
     * @param type the type of file (ex. plugins, world)
     */
    public void uploadFile(java.io.File file, String type) throws Exception {
        try {
            resetRanges();

            String destination = getDestination();

            ArrayList<String> typeFolders = new ArrayList<>();
            Collections.addAll(typeFolders, destination.split(java.io.File.separator.replace("\\", "\\\\")));
            Collections.addAll(typeFolders, type.split(java.io.File.separator.replace("\\", "\\\\")));

            File folder = null;

            for (String typeFolder : typeFolders) {
                if (typeFolder.equals(".") || typeFolder.equals("..")) {
                    continue;
                }

                if (folder == null) {
                    folder = createFolder(typeFolder);
                } else {
                    folder = createFolder(typeFolder, folder);
                }
            }

            Request request = new Request.Builder()
                    .addHeader("Authorization", "Bearer " + returnAccessToken())
                    .url("https://graph.microsoft.com/v1.0/me/drive/root:/" + folder.getPath() + "/" + file.getName() + ":/createUploadSession")
                    .post(RequestBody.create("{}", jsonMediaType))
                    .build();

            Response response = httpClient.newCall(request).execute();
            JSONObject parsedResponse = new JSONObject(response.body().string());
            response.close();

            String uploadURL = parsedResponse.getString("uploadUrl");

            //Assign our backup to Random Access File
            raf = new RandomAccessFile(file, "r");

            boolean isComplete = false;

            while (!isComplete) {
                byte[] bytesToUpload = getChunk();

                request = new Request.Builder()
                        .addHeader("Content-Range", String.format("bytes %d-%d/%d", getTotalUploaded(), getTotalUploaded() + bytesToUpload.length - 1, file.length()))
                        .url(uploadURL)
                        .put(RequestBody.create(bytesToUpload, zipMediaType))
                        .build();

                response = httpClient.newCall(request).execute();

                if (getTotalUploaded() + bytesToUpload.length < file.length()) {
                    try {
                        parsedResponse = new JSONObject(response.body().string());
                        List<String> nextExpectedRanges = (List<String>) (Object) parsedResponse.getJSONArray("nextExpectedRanges").toList();
                        setRanges(nextExpectedRanges.toArray(new String[nextExpectedRanges.size()]));
                    } catch (NullPointerException e) {
                        log.error(e.getMessage(), e);
                    }
                } else {
                    isComplete = true;
                }

                response.close();
            }

            deleteFiles(folder);
        } catch (UnknownHostException exception) {
            log.error("Failed to upload backup to OneDrive, check your network connection", "drivebackup.linkAccounts", true);
            setErrorOccurred(true);
        } catch (Exception error) {
            log.error(error.getMessage(), error);
            setErrorOccurred(true);
        }

        raf.close();
    }

    /**
     * Gets whether an error occurred while accessing the authenticated user's OneDrive
     *
     * @return whether an error occurred
     */
    public boolean isErrorWhileUploading() {
        return this.errorOccurred;
    }

    /**
     * closes any remaining connectionsretrieveNewAccessToken
     */
    public void close() {
        return; // nothing needs to be done
    }

    /**
     * Gets the name of this upload service
     *
     * @return name of upload service
     */
    public String getName() {
        return "OneDrive";
    }


    /**
     * Creates a folder with the specified name in the specified parent folder in the authenticated user's OneDrive
     *
     * @param name   the name of the folder
     * @param parent the parent folder
     * @return the created folder
     * @throws Exception
     */
    private File createFolder(String name, File parent) throws Exception {
        File file = getFolder(name, parent);
        if (file != null) {
            return file;
        }

        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + returnAccessToken())
                .url("https://graph.microsoft.com/v1.0/me/drive/root:/" + parent.getPath())
                .build();

        Response response = httpClient.newCall(request).execute();
        JSONObject parsedResponse = new JSONObject(response.body().string());
        response.close();

        String parentId = parsedResponse.getString("id");

        RequestBody requestBody = RequestBody.create(
                "{" +
                        " \"name\": \"" + name + "\"," +
                        " \"folder\": {}," +
                        " \"@name.conflictBehavior\": \"fail\"" +
                        "}", jsonMediaType);

        request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + returnAccessToken())
                .url("https://graph.microsoft.com/v1.0/me/drive/items/" + parentId + "/children")
                .post(requestBody)
                .build();

        response = httpClient.newCall(request).execute();
        boolean folderCreated = response.isSuccessful();
        response.close();

        if (!folderCreated) {
            throw new Exception("Couldn't create folder " + name);
        }

        return parent.add(name);
    }

    /**
     * Creates a folder with the specified name in the root of the authenticated user's OneDrive
     *
     * @param name the name of the folder
     * @return the created folder
     * @throws Exception
     */
    private File createFolder(String name) throws Exception {
        File file = getFolder(name);
        if (file != null) {
            return file;
        }

        RequestBody requestBody = RequestBody.create(
                "{" +
                        " \"name\": \"" + name + "\"," +
                        " \"folder\": {}," +
                        " \"@name.conflictBehavior\": \"fail\"" +
                        "}", jsonMediaType);

        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + returnAccessToken())
                .url("https://graph.microsoft.com/v1.0/me/drive/root/children")
                .post(requestBody)
                .build();

        Response response = httpClient.newCall(request).execute();
        boolean folderCreated = response.isSuccessful();
        response.close();

        if (!folderCreated) {
            throw new Exception("Couldn't create folder " + name);
        }

        return new File().add(name);
    }

    /**
     * Returns the folder in the specified parent folder of the authenticated user's OneDrive with the specified name
     *
     * @param name   the name of the folder
     * @param parent the parent folder
     * @return the folder or {@code null}
     */
    private File getFolder(String name, File parent) {
        try {
            Request request = new Request.Builder()
                    .addHeader("Authorization", "Bearer " + returnAccessToken())
                    .url("https://graph.microsoft.com/v1.0/me/drive/root:/" + parent.getPath() + ":/children")
                    .build();

            Response response = httpClient.newCall(request).execute();
            JSONObject parsedResponse = new JSONObject(response.body().string());
            response.close();

            JSONArray jsonArray = parsedResponse.getJSONArray("value");

            for (int i = 0; i < jsonArray.length(); i++) {
                String folderName = jsonArray.getJSONObject(i).getString("name");

                if (name.equals(folderName)) {
                    return parent.add(name);
                }
            }

        } catch (Exception exception) {
        }

        return null;
    }

    /**
     * Returns the folder in the root of the authenticated user's OneDrive with the specified name
     *
     * @param name the name of the folder
     * @return the folder or {@code null}
     */
    private File getFolder(String name) {
        try {
            Request request = new Request.Builder()
                    .addHeader("Authorization", "Bearer " + returnAccessToken())
                    .url("https://graph.microsoft.com/v1.0/me/drive/root/children")
                    .build();

            Response response = httpClient.newCall(request).execute();
            JSONObject parsedResponse = new JSONObject(response.body().string());
            response.close();

            JSONArray jsonArray = parsedResponse.getJSONArray("value");

            for (int i = 0; i < jsonArray.length(); i++) {
                String folderName = jsonArray.getJSONObject(i).getString("name");

                if (name.equals(folderName)) {
                    return new File().add(name);
                }
            }

        } catch (Exception exception) {
        }

        return null;
    }

    /**
     * Deletes the oldest files in the specified folder past the number to retain from the authenticated user's OneDrive
     * <p>
     * The number of files to retain is specified by the user in the {@code config.yml}
     *
     */
    private void deleteFiles(File parent) throws Exception {
        int fileLimit = keepCount;

        if (fileLimit == -1) {
            return;
        }

        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + returnAccessToken())
                .url("https://graph.microsoft.com/v1.0/me/drive/root:/" + parent.getPath() + ":/children?sort_by=createdDateTime")
                .build();

        Response response = httpClient.newCall(request).execute();
        JSONObject parsedResponse = new JSONObject(response.body().string());
        response.close();

        ArrayList<String> availableFileIDs = new ArrayList<>();

        JSONArray jsonArray = parsedResponse.getJSONArray("value");
        for (int i = 0; i < jsonArray.length(); i++) {
            availableFileIDs.add(jsonArray.getJSONObject(i).getString("id"));
        }

        if (fileLimit < availableFileIDs.size()) {
            log.error("There are " + availableFileIDs.size() + " file(s) which exceeds the limit of " + fileLimit + ", deleting");
        }

        for (Iterator<String> iterator = availableFileIDs.listIterator(); iterator.hasNext(); ) {
            String fileIDValue = iterator.next();
            if (fileLimit < availableFileIDs.size()) {
                request = new Request.Builder()
                        .addHeader("Authorization", "Bearer " + returnAccessToken())
                        .url("https://graph.microsoft.com/v1.0/me/drive/items/" + fileIDValue)
                        .delete()
                        .build();

                httpClient.newCall(request).execute().close();

                iterator.remove();
            }

            if (availableFileIDs.size() <= fileLimit) {
                break;
            }
        }
    }

    /**
     * A file/folder in the authenticated user's OneDrive
     */
    private static final class File {
        private ArrayList<String> filePath = new ArrayList<>();

        /**
         * Creates a reference of the {@code File} object
         */
        File() {
        }

        /**
         * Returns a {@code File} with the specified folder added to the file path
         *
         * @param folder the {@code File}
         */
        private File add(String folder) {
            File childFile = new File();
            if (getPath().isEmpty()) {
                childFile.setPath(folder);
            } else {
                childFile.setPath(getPath() + "/" + folder);
            }

            return childFile;
        }

        /**
         * Sets the path of the file/folder
         *
         * @param path the path, as an {@code String}
         */
        private void setPath(String path) {
            filePath.clear();
            Collections.addAll(filePath, path.split("/"));
        }

        /**
         * Gets the path of the file/folder
         *
         * @return the path, as a {@code String}
         */
        private String getPath() {
            return String.join("/", filePath);
        }

        /**
         * Gets the name of the file/folder
         *
         * @return the name, including any file extensions
         */
        private String getName() {
            return filePath.get(filePath.size() - 1);
        }

        /**
         * Gets the path of the parent folder of the file/folder
         *
         * @return the path, as a String
         */
        private String getParent() {
            ArrayList<String> parentPath = new ArrayList<>(filePath);
            parentPath.remove(parentPath.size() - 1);

            return String.join("/", parentPath);
        }
    }

    /**
     * A range of bytes
     */
    private static class Range {
        private final long start;
        private final long end;

        /**
         * Creates an instance of the {@code Range} object
         *
         * @param start the index of the first byte
         * @param end   the index of the last byte
         */
        private Range(long start, long end) {
            this.start = start;
            this.end = end;
        }
    }

    /**
     * Resets the number of bytes uploaded in the last chunk and the number of bytes uploaded in total
     */
    private void resetRanges() {
        lastUploaded = 0;
        totalUploaded = 0;
    }

    /**
     * Sets the number of bytes uploaded in the last chunk and the number of bytes uploaded in total from the ranges of bytes the OneDrive API requested to be uploaded last
     *
     * @param stringRanges the ranges of bytes requested
     */
    private void setRanges(String[] stringRanges) {
        Range[] ranges = new Range[stringRanges.length];
        for (int i = 0; i < stringRanges.length; i++) {
            long start = Long.parseLong(stringRanges[i].substring(0, stringRanges[i].indexOf('-')));

            String s = stringRanges[i].substring(stringRanges[i].indexOf('-') + 1);

            long end = 0;
            if (!s.isEmpty()) {
                end = Long.parseLong(s);
            }

            ranges[i] = new Range(start, end);
        }

        if (ranges.length > 0) {
            lastUploaded = ranges[0].start - totalUploaded;
            totalUploaded = ranges[0].start;
        }
    }

    /**
     * Gets an array of bytes to upload next from the file buffer based on the number of bytes uploaded so far
     *
     * @return the array of bytes
     * @throws IOException
     */
    private byte[] getChunk() throws IOException {

        byte[] bytes = new byte[CHUNK_SIZE];

        raf.seek(totalUploaded);
        int read = raf.read(bytes);

        if (read < CHUNK_SIZE) {
            bytes = Arrays.copyOf(bytes, read);
        }

        return bytes;
    }


    /**
     * Formats the specified number of bytes as a readable file size
     *
     * @param size the number of bytes
     * @return a {@code String} containing the readable file size
     */
    private static String readableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * Sets whether an error occurred while accessing the authenticated user's OneDrive
     *
     * @param errorOccurredValue whether an error occurred
     */
    private void setErrorOccurred(boolean errorOccurredValue) {
        this.errorOccurred = errorOccurredValue;
    }

    /**
     * Gets the number of bytes uploaded in total
     *
     * @return the number of bytes
     */
    private long getTotalUploaded() {
        return totalUploaded;
    }

    /**
     * Gets the number of bytes uploaded in the last chunk
     *
     * @return the number of bytes
     */
    private long getLastUploaded() {
        return lastUploaded;
    }

    /**
     * Sets the access token of the authenticated user
     *
     * @param accessTokenValue the access token
     */
    private void setAccessToken(String accessTokenValue) {
        this.accessToken = accessTokenValue;
    }

    /**
     * Sets the refresh token of the authenticated user
     *
     * @param refreshTokenValue the refresh token
     */
    private void setRefreshToken(String refreshTokenValue) {
        this.refreshToken = refreshTokenValue;
    }

    /**
     * Gets the access token of the authenticated user
     *
     * @return the access token
     */
    private String returnAccessToken() {
        return this.accessToken;
    }

    /**
     * Gets the refresh token of the authenticated user
     *
     * @return the refresh token
     */
    private String returnRefreshToken() {
        return this.refreshToken;
    }
}
