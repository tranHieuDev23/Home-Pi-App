package com.hieutm.homepi.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.annotations.NonNull;
import kotlin.text.Charsets;

public class BluetoothHelper {
    private BluetoothHelper() {
    }

    private static final BluetoothHelper INSTANCE = new BluetoothHelper();

    public static synchronized BluetoothHelper getInstance() {
        return INSTANCE;
    }

    public boolean isBluetoothEnabled() {
        try {
            getBluetoothAdapter();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<BluetoothDevice> getPairedDevices() {
        BluetoothAdapter adapter;
        try {
            adapter = getBluetoothAdapter();
        } catch (Exception e) {
            return new ArrayList<>();
        }
        return new ArrayList<>(adapter.getBondedDevices());
    }

    public void startDiscovering() {
        getBluetoothAdapter().startDiscovery();
    }

    public void stopDiscovering() {
        getBluetoothAdapter().cancelDiscovery();
    }

    public Single<BluetoothCommunicationHandler> connect(String mac) {
        return new Single<BluetoothCommunicationHandler>() {
            @Override
            protected void subscribeActual(@NonNull SingleObserver<? super BluetoothCommunicationHandler> observer) {
                BluetoothAdapter adapter;
                try {
                    adapter = getBluetoothAdapter();
                } catch (Exception e) {
                    observer.onError(e);
                    return;
                }
                adapter.cancelDiscovery();
                BluetoothDevice device = adapter.getRemoteDevice(mac);

                BluetoothSocket socket;
                try {
                    socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                } catch (IOException e) {
                    observer.onError(new RuntimeException("Exception happened while creating RFCOMM socket", e));
                    return;
                }

                try {
                    socket.connect();
                } catch (IOException e) {
                    try {
                        socket.close();
                    } catch (IOException ioException) {
                        observer.onError(new RuntimeException("Failed to open RFCOMM socket and close it afterwards", e));
                        return;
                    }
                    observer.onError(new RuntimeException("Failed to open RFCOMM socket, but successfully closed it", e));
                    return;
                }

                BluetoothCommunicationHandler handler;
                try {
                    handler = new BluetoothCommunicationHandler(socket);
                } catch (IOException e) {
                    observer.onError(new RuntimeException("Failed to open input and output stream from socket", e));
                    return;
                }
                observer.onSuccess(handler);
            }
        };
    }

    public static class BluetoothCommunicationHandler {
        public static final int DEFAULT_BUFFER_SIZE = 1024;
        public static final long DEFAULT_TIMEOUT = 10000;

        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;
        private boolean isSendingMessage;

        private BluetoothCommunicationHandler(BluetoothSocket socket) throws IOException {
            this.socket = socket;
            this.inputStream = socket.getInputStream();
            this.outputStream = socket.getOutputStream();
            this.isSendingMessage = false;
        }

        public Single<String> sendMessage(@NotNull String message) {
            return sendMessage(message, DEFAULT_TIMEOUT, DEFAULT_BUFFER_SIZE);
        }

        public Single<String> sendMessage(@NotNull String message, long timeout) {
            return sendMessage(message, timeout, DEFAULT_BUFFER_SIZE);
        }

        public Single<String> sendMessage(@NotNull String message, long timeout, int bufferSize) {
            return new Single<String>() {
                @Override
                protected void subscribeActual(@NonNull SingleObserver<? super String> observer) {
                    if (isSendingMessage) {
                        observer.onError(new RuntimeException("Another message is being sent"));
                        return;
                    }
                    isSendingMessage = true;
                    try {
                        outputStream.write(message.getBytes());
                        outputStream.write('\n');
                    } catch (IOException e) {
                        isSendingMessage = false;
                        observer.onError(new RuntimeException("Exception happened while sending the message"));
                        return;
                    }
                    byte[] buffer = new byte[bufferSize];
                    int currentPosition = 0;
                    long startTime = System.currentTimeMillis();
                    while (true) {
                        if (System.currentTimeMillis() - startTime > timeout) {
                            isSendingMessage = false;
                            observer.onError(new RuntimeException("Response timeout"));
                            return;
                        }
                        try {
                            currentPosition += inputStream.read(buffer, currentPosition, inputStream.available());
                            if (currentPosition > 0 && buffer[currentPosition - 1] == '\n') {
                                break;
                            }
                        } catch (IOException e) {
                            isSendingMessage = false;
                            observer.onError(new RuntimeException("Exception happened while reading the response"));
                            return;
                        }
                    }
                    String response = new String(buffer, Charsets.UTF_8);
                    isSendingMessage = false;
                    observer.onSuccess(response);
                }
            };
        }

        public void disconnect() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private BluetoothAdapter getBluetoothAdapter() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            throw new RuntimeException("Bluetooth is not supported on this device");
        }
        if (!adapter.isEnabled()) {
            throw new RuntimeException("Bluetooth is not yet enabled on this device");
        }
        return adapter;
    }
}
