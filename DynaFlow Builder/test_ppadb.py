from ppadb.client import Client as AdbClient

# Default is "127.0.0.1" and 5037
client = AdbClient(host="127.0.0.1", port=5037)
print(client.devices()[0].serial)

device = client.device("emulator-5578")
device.pull()