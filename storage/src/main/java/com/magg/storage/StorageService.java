package com.magg.storage;

import com.magg.api.dto.FileDTO;
import com.magg.api.exception.FileNotFoundException;
import java.io.File;
import java.io.InputStream;
import java.util.Optional;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

/**
 * External Storage interface.
 */
public interface StorageService {

    /**
     * Creates an asset in an external storage service.
     *
     * @param file      the asset to put
     * @param assetType asset type
     * @return path for the asset
     */
    String createAsset(File file, AssetType assetType);

    /**
     *
     * @param name
     * @param assetType
     * @return
     */

    InputStream downloadAsset(String name, AssetType assetType);

    Optional<FilePointer> findFile(FileDTO fileDTO);


    default Resource prepareResponse(FilePointer filePointer) {
        final InputStream inputStream = filePointer.open();
        return new InputStreamResource(inputStream);
    }

    default Resource notFound() {
        throw new FileNotFoundException();
    }

    void upload(InputStream in, AssetType assetType, String contentType, String name);

    Long retrieveContentLength(String name);

    void upload2(InputStream in, AssetType assetType, String contentType, String name);

    void upload3(InputStream in, AssetType assetType, String contentType, String name, String id);
}
