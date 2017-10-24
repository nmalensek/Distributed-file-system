package cs555.dfs.util;

public class ChunkMetadata {

    private String chunkName;
    private int versionNumber;
    private String fileName;
    private long lastUpdatedTime;

    public ChunkMetadata(String chunkName, int versionNumber, String fileName, long lastUpdatedTime) {
        this.chunkName = chunkName;
        this.versionNumber = versionNumber;
        this.fileName = fileName;
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public String getChunkName() { return chunkName; }

    public void setChunkName(String chunkName) {
        this.chunkName = chunkName;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(long lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    @Override
    public String toString() {
        return chunkName + ":" + versionNumber + ":" + fileName + ":" + lastUpdatedTime;
    }
}
