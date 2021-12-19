package moe.ore.memory.data;

import android.os.Parcel;
import android.os.Parcelable;

public class PMAP implements Parcelable {
    public long addr;
    public long taddr;
    // public PMAP next;

    protected PMAP(Parcel in) {
        addr = in.readLong();
        taddr = in.readLong();
    }

    protected PMAP(int addr, int taddr) {
        this.addr = addr;
        this.taddr = taddr;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(addr);
        dest.writeLong(taddr);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PMAP> CREATOR = new Creator<PMAP>() {
        @Override
        public PMAP createFromParcel(Parcel in) {
            return new PMAP(in);
        }

        @Override
        public PMAP[] newArray(int size) {
            return new PMAP[size];
        }
    };
}
