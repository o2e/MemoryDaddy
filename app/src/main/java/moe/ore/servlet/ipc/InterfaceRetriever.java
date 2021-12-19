package moe.ore.servlet.ipc;

import android.os.IBinder;
import android.os.IInterface;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class InterfaceRetriever<T> {
        /**
         * Stability: stable, as changes to this pattern in AOSP would probably require all
         * AIDL-using apps to be recompiled.
         *
         * @param clazz Class of T
         * @param binder Binder proxy to retrieve interface from
         * @return T (proxy) instance or null
         */
        T getInterfaceFromBinder(Class<T> clazz, IBinder binder) {
            // There does not appear to be a nice way to do this without reflection,
            // though of course you can use T.Stub.asInterface(binder) in final code, that doesn't
            // help for our callbacks
            try {
                Class<?> cStub = Class.forName(clazz.getName() + "$Stub");
                Field fDescriptor = cStub.getDeclaredField("DESCRIPTOR");
                fDescriptor.setAccessible(true);

                String descriptor = (String)fDescriptor.get(binder);
                IInterface intf = binder.queryLocalInterface(descriptor);
                if (clazz.isInstance(intf)) {
                    // local
                    return (T)intf;
                } else {
                    // remote
                    Class<?> cProxy = Class.forName(clazz.getName() + "$Stub$Proxy");
                    Constructor<?> ctorProxy = cProxy.getDeclaredConstructor(IBinder.class);
                    ctorProxy.setAccessible(true);
                    return (T)ctorProxy.newInstance(binder);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }