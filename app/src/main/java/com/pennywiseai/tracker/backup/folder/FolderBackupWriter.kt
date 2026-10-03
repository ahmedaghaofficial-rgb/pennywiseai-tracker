package com.pennywiseai.tracker.backup.folder

import android.content.Context
import android.net.Uri
import com.pennywiseai.tracker.R
import com.pennywiseai.tracker.core.localization.AppLocaleController
import androidx.documentfile.provider.DocumentFile
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderBackupWriter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private fun uiText(id: Int, vararg args: Any): String = AppLocaleController.wrap(context).getString(id, *args)
    fun canWriteToFolder(treeUri: String): Boolean {
        val folder = DocumentFile.fromTreeUri(context, Uri.parse(treeUri)) ?: return false
        if (!folder.isDirectory || !folder.canWrite()) return false

        val existing = folder.findFile(ScheduledFolderBackupConstants.BACKUP_FILE_NAME)
        return existing == null || existing.canWrite()
    }

    fun writeBackup(treeUri: String, bytes: ByteArray): Result {
        val folder = DocumentFile.fromTreeUri(context, Uri.parse(treeUri))
            ?: return Result.Failure(uiText(R.string.flosi_backup_folder_inaccessible))

        if (!folder.isDirectory || !folder.canWrite()) {
            return Result.Failure(uiText(R.string.flosi_cannot_write_backup_folder))
        }

        // Write to a sibling temp file first, then swap it into place. SAF has no atomic
        // replace, so writing straight to the backup file would truncate the previous
        // (good) backup on open and leave nothing behind if the write is interrupted.
        folder.findFile(ScheduledFolderBackupConstants.TEMP_BACKUP_FILE_NAME)?.delete()
        val tempFile = folder.createFile(
            "application/octet-stream",
            ScheduledFolderBackupConstants.TEMP_BACKUP_FILE_NAME
        ) ?: return Result.Failure(uiText(R.string.flosi_backup_file_create_failed))

        val writeError = writeBytes(tempFile.uri, bytes)
        if (writeError != null) {
            tempFile.delete()
            return Result.Failure(writeError)
        }

        // Temp now holds a complete backup. Remove the old file, then rename temp -> final.
        // If interrupted between the two, the fresh data still exists under the temp name.
        folder.findFile(ScheduledFolderBackupConstants.BACKUP_FILE_NAME)?.delete()
        if (tempFile.renameTo(ScheduledFolderBackupConstants.BACKUP_FILE_NAME)) {
            return Result.Success
        }

        // Some SAF providers don't support rename. Fall back to a direct write of the
        // final file; the temp is left in place as a recoverable copy on failure.
        val finalFile = folder.createFile(
            "application/octet-stream",
            ScheduledFolderBackupConstants.BACKUP_FILE_NAME
        ) ?: return Result.Failure(uiText(R.string.flosi_backup_file_finalize_failed))

        val finalError = writeBytes(finalFile.uri, bytes)
        if (finalError != null) {
            return Result.Failure(finalError)
        }
        tempFile.delete()
        return Result.Success
    }

    /** Writes [bytes] to [uri], truncating. Returns null on success or an error message. */
    private fun writeBytes(uri: Uri, bytes: ByteArray): String? {
        return try {
            context.contentResolver.openOutputStream(uri, "wt")?.use { stream ->
                stream.write(bytes)
            } ?: uiText(R.string.flosi_backup_file_open_failed)
            null
        } catch (e: Exception) {
            uiText(R.string.flosi_backup_file_write_failed, e.message ?: uiText(R.string.flosi_unknown_error))
        }
    }

    sealed class Result {
        data object Success : Result()
        data class Failure(val message: String) : Result()
    }
}
