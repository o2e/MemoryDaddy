package moe.ore.memory.data;

import android.os.Parcel;
import android.os.Parcelable;

public class ProcessInfo implements Parcelable {
    public String name; // 进程名 com.qq:MSF
    public String processName; // 进程的名字 QQ
    public int pid;

    protected ProcessInfo(Parcel in) {
        name = in.readString();
        processName = in.readString();
        pid = in.readInt();
    }

    public ProcessInfo(String name, String processName, int pid) {
        this.name = name;
        this.processName = processName;
        this.pid = pid;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(processName);
        dest.writeInt(pid);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ProcessInfo> CREATOR = new Creator<ProcessInfo>() {
        @Override
        public ProcessInfo createFromParcel(Parcel in) {
            return new ProcessInfo(in);
        }

        @Override
        public ProcessInfo[] newArray(int size) {
            return new ProcessInfo[size];
        }
    };
}
