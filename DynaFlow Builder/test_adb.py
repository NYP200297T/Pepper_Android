from adb_shell.auth.keygen import keygen
from adb_shell.adb_device import AdbDeviceTcp, AdbDevice
from adb_shell.transport import 
from adb_shell.auth.sign_pythonrsa import PythonRSASigner

import os

android_path = "storage/emulated/0"
adb_key_dir = str(os.path.join(os.getcwd(), "adb_key"))
adb_key =  str(os.path.join(adb_key_dir, "key"))
if not os.path.exists(adb_key_dir):
    os.mkdir(adb_key_dir)
if not os.path.exists(adb_key):
    keygen(os.path.join(adb_key))

with open(adb_key) as f:
    priv = f.read()
with open(adb_key + '.pub') as f:
    pub = f.read()
signer = PythonRSASigner(pub, priv)

emulator_adb_key = os.path.join(os.path.expanduser('~'), ".emulator_console_auth_token")

# for i in range(5555, 5586, 2):
#     testdevice = AdbDeviceTcp("127.0.0.1", port=i)
#     try:
#         print(i)
#         # connected = testdevice.connect(rsa_keys=[signer], auth_timeout_s=5)
#         connected = AdbDevice()
#     except ConnectionRefusedError:
#         testdevice.close()
#         continue

#     if connected:
#         break

testdevice = AdbDeviceTcp("127.0.0.1", port=5037)
connected = testdevice.connect(rsa_keys=[signer], auth_timeout_s=5)

print(connected)
if connected:
    testdevice.close()