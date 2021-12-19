// IRootIPC.aidl
package moe.ore.memory;

interface IRootIPC {
    void hello(IBinder self);

    IBinder getUserIPC();

    void bye(IBinder self);
}