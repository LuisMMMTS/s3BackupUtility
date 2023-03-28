The runnable example can be found in the tags section

The initial approach with zip creation and the subsequent one with streamswere followed although not completely finished in order to:
- Maintain file permissions
- Maintain folder structure even for empty folders
- Maintain owner record
- maintain creation and modification timestamps
- Being able to both upload and download without exceeding ram limits or having to create temporary files



This can be improved in some ways:
- drop the zip creation and work with real BLOBs
- working with streams (not using the readAllBytes[] method) to avoid the need of having extra disk space and or RAM space both uploading and downloading since the OS might shut down the process and the system might become unusable in a multiple gigabyte situation
- not using Java's serializer but rather develop the serialization function because any updates on the code will render older version backups useless
- retry on upload failure
- not needing to download everything when looking for one file
- refactoring of the code
- usage of threads for the time intensive task of uploading/downloading whilst streaming the local data in another thread
- usage of checksum functions provided by aws SDK