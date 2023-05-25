import customtkinter
import os
from adb_shell.auth.keygen import keygen
from adb_shell.adb_device import AdbDeviceTcp
from adb_shell.auth.sign_pythonrsa import PythonRSASigner
import shutil
from tkinter.messagebox import askyesno

# THIS IS A WORKAROUND PROGRAM IF COMPUTER CANNOT/DOES NOT HAVE ADB
# DOES NOT INCLUDE EMULATOR CONNNECTIVITY

package_name = "org.ishoot.dynaflow"

class AdbFrame(customtkinter.CTkFrame):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)

        self.local_dyna_json = os.path.join(os.getcwd(), "DynaFlow", "flow.json")
        self.local_dyna_images = os.path.join(os.getcwd(), "DynaFlow", "images")
        self.local_map = os.path.join(os.getcwd(), "Maps", "points.json")
        if not os.path.exists(os.path.join(os.getcwd(), "DynaFlow")):
            os.mkdir(os.path.join(os.getcwd(), "DynaFlow"))
        
        # first setup of adb
        self.android_path = "storage/emulated/0"
        self.adb_key_dir = str(os.path.join(os.getcwd(), "adb_key"))
        self.adb_key =  str(os.path.join(self.adb_key_dir, "key"))
        if not os.path.exists(self.adb_key_dir):
            os.mkdir(self.adb_key_dir)
        if not os.path.exists(self.adb_key):
            keygen(os.path.join(self.adb_key))

        with open(self.adb_key) as f:
            priv = f.read()
        with open(self.adb_key + '.pub') as f:
            pub = f.read()
        self.signer = PythonRSASigner(pub, priv)

        self.grid_columnconfigure(0, weight=1)
        self.grid_rowconfigure((0,1,2,3,4), weight=1)

        self.adb_label = customtkinter.CTkLabel(self, text="Disconnected", anchor="center")
        self.adb_label.grid(row=0, column=0, padx=10, pady=10)

        self.progresser = customtkinter.CTkProgressBar(self, orientation="horizontal")
        self.progresser.grid(row=1, column=0, padx=10, pady=10)
        self.progresser.set(0)

        self.connect_frame = customtkinter.CTkFrame(self)

        # IP Address and connect
        self.ipaddress1 = customtkinter.StringVar()
        self.ipaddress2 = customtkinter.StringVar()
        self.ipaddress3 = customtkinter.StringVar()
        self.ipaddress4 = customtkinter.StringVar()

        self.ipaddress_frame = customtkinter.CTkFrame(self.connect_frame)
        self.ipaddress_frame.grid_rowconfigure(0, weight=1)
        self.ipaddress_frame.grid_columnconfigure((0,1,2,3,4,5,6), weight=1)
        
        self.ipaddress1_entry = customtkinter.CTkEntry(self.ipaddress_frame, width=40, textvariable=self.ipaddress1, justify="center")
        self.ipaddress1_entry.grid(row=0, column=0, sticky="ew")
        self.ipaddress1_dot = customtkinter.CTkLabel(self.ipaddress_frame, width=5, text=".")
        self.ipaddress1_dot.grid(row=0, column=1, sticky="ew")
        self.ipaddress2_entry = customtkinter.CTkEntry(self.ipaddress_frame, width=40, textvariable=self.ipaddress2, justify="center")
        self.ipaddress2_entry.grid(row=0, column=2, sticky="ew")
        self.ipaddress2_dot = customtkinter.CTkLabel(self.ipaddress_frame, width=5, text=".")
        self.ipaddress2_dot.grid(row=0, column=3, sticky="ew")
        self.ipaddress3_entry = customtkinter.CTkEntry(self.ipaddress_frame, width=40, textvariable=self.ipaddress3, justify="center")
        self.ipaddress3_entry.grid(row=0, column=4, sticky="ew")
        self.ipaddress3_dot = customtkinter.CTkLabel(self.ipaddress_frame, width=5, text=".")
        self.ipaddress3_dot.grid(row=0, column=5, sticky="ew")
        self.ipaddress4_entry = customtkinter.CTkEntry(self.ipaddress_frame, width=40, textvariable=self.ipaddress4, justify="center")
        self.ipaddress4_entry.grid(row=0, column=6, sticky="ew")
        
        self.ipaddress1.trace("w", lambda *args: self.limit_spaces(self.ipaddress1))
        self.ipaddress2.trace("w", lambda *args: self.limit_spaces(self.ipaddress2))
        self.ipaddress3.trace("w", lambda *args: self.limit_spaces(self.ipaddress3))
        self.ipaddress4.trace("w", lambda *args: self.limit_spaces(self.ipaddress4))

        self.ipaddress_frame.grid(row=0, column=0, sticky="nsew", padx=10, pady=10)

        self.connect_button = customtkinter.CTkSwitch(self.connect_frame, text="Connect", command=self.connect_pepper)
        self.connect_button.grid(row=0, column=1, sticky="nsew", padx=10, pady=10)

        self.connect_frame.grid(row=2, column=0, padx=10, pady=10)

        self.dynaflow_path_frame = customtkinter.CTkFrame(self)
        self.dynaflow_path_frame.grid_rowconfigure(0, weight=1)
        self.dynaflow_path_frame.grid_columnconfigure(0, weight=1)
        
        self.dynaflow_path_var = customtkinter.StringVar(value=self.android_path+"/DynaFlow/flow.json")
        self.dynaflow_path_entry = customtkinter.CTkEntry(self.dynaflow_path_frame, textvariable=self.dynaflow_path_var)
        self.dynaflow_path_entry.grid(row=0, column=0, sticky="nsew", padx=5, pady=5)
        self.dynaflow_path_pull = customtkinter.CTkButton(self.dynaflow_path_frame, width=50, text="Pull", command=self.get_dynaflow)
        self.dynaflow_path_pull.grid(row=0, column=1, padx=5, pady=5)
        self.dynaflow_path_push = customtkinter.CTkButton(self.dynaflow_path_frame, width=50, text="Push", command=self.send_dynaflow)
        self.dynaflow_path_push.grid(row=0, column=2, padx=5, pady=5)

        self.dynaflow_path_frame.grid(row=3, column=0, sticky="nsew", padx=10, pady=2)

        self.dynaflow_image_frame = customtkinter.CTkFrame(self)
        self.dynaflow_image_frame.grid_rowconfigure(0, weight=1)
        self.dynaflow_image_frame.grid_columnconfigure(0, weight=1)
        
        self.dynaflow_image_var = customtkinter.StringVar(value=self.android_path+"/DynaFlow/images/")
        self.dynaflow_image_entry = customtkinter.CTkEntry(self.dynaflow_image_frame, textvariable=self.dynaflow_image_var)
        self.dynaflow_image_entry.grid(row=0, column=0, sticky="nsew", padx=5, pady=5)
        self.dynaflow_image_pull = customtkinter.CTkButton(self.dynaflow_image_frame, width=50, text="Pull", command=self.get_dynaflow_images)
        self.dynaflow_image_pull.grid(row=0, column=1, padx=5, pady=5)
        self.dynaflow_image_push = customtkinter.CTkButton(self.dynaflow_image_frame, width=50, text="Push", command=self.send_dynaflow_images)
        self.dynaflow_image_push.grid(row=0, column=2, padx=5, pady=5)

        self.dynaflow_image_frame.grid(row=4, column=0, sticky="nsew", padx=10, pady=2)

        self.map_path_frame = customtkinter.CTkFrame(self)
        self.map_path_frame.grid_rowconfigure(0, weight=1)
        self.map_path_frame.grid_columnconfigure(0, weight=1)
        
        self.map_path_var = customtkinter.StringVar(value=self.android_path+"/Maps/points.json")
        self.map_path_entry = customtkinter.CTkEntry(self.map_path_frame, textvariable=self.map_path_var)
        self.map_path_entry.grid(row=0, column=0, sticky="nsew", padx=5, pady=5)
        self.map_path_pull = customtkinter.CTkButton(self.map_path_frame, text="Pull", width=50, command=self.get_map)
        self.map_path_pull.grid(row=0, column=1, columnspan=2, padx=5, pady=5)
        # self.map_path_push = customtkinter.CTkButton(self.map_path_frame, text="Push", width=50, command=self.send_map)
        # self.map_path_push.grid(row=0, column=2, padx=5, pady=5)

        self.map_path_frame.grid(row=5, column=0, sticky="nsew", padx=10, pady=5)

        self.open_dynaflow_button = customtkinter.CTkButton(self, text="Open DynaFlow", command=self.open_dynaflow)
        self.open_dynaflow_button.grid(row=6, column=0, sticky="nsew", padx=10, pady=5)

        self.dynaflow_path_entry.configure(state="disabled")
        self.map_path_entry.configure(state="disabled")
        self.dynaflow_path_pull.configure(state="disabled")
        self.dynaflow_path_push.configure(state="disabled")
        self.dynaflow_image_pull.configure(state="disabled")
        self.dynaflow_image_push.configure(state="disabled")
        self.map_path_pull.configure(state="disabled")
        self.open_dynaflow_button.configure(state='disabled')
        # self.map_path_push.configure(state="disabled")

    def connect_pepper(self):
        if bool(self.connect_button.get()):
            
            if len(self.ipaddress1.get()) > 0 and len(self.ipaddress2.get()) > 0 and len(self.ipaddress3.get()) > 0 and len(self.ipaddress4.get()) > 0:
                # Connect
                self.pepper = AdbDeviceTcp(f'{self.ipaddress1.get()}.{self.ipaddress2.get()}.{self.ipaddress3.get()}.{self.ipaddress4.get()}', 5555, default_transport_timeout_s=10)
                try:
                    connected = self.pepper.connect(rsa_keys=[self.signer], auth_timeout_s=10)
                except Exception as e:
                    print(e)
                    connected = False
                
                if connected:
                    self.adb_label.configure(text="Connected")
                    self.ipaddress1_entry.configure(state="disabled")
                    self.ipaddress2_entry.configure(state="disabled")
                    self.ipaddress3_entry.configure(state="disabled")
                    self.ipaddress4_entry.configure(state="disabled")

                    self.dynaflow_path_entry.configure(state="normal")
                    self.map_path_entry.configure(state="normal")
                    self.dynaflow_path_pull.configure(state="normal")
                    self.dynaflow_path_push.configure(state="normal")
                    self.dynaflow_image_pull.configure(state="normal")
                    self.dynaflow_image_push.configure(state="normal")
                    self.map_path_pull.configure(state="normal")
                    self.open_dynaflow_button.configure(state='normal')
                    # self.map_path_push.configure(state="normal")
                else:
                    self.adb_label.configure(text="Error, not connected")
                    self.connect_button.deselect()

        else:
            self.pepper.close()
            self.ipaddress1_entry.configure(state="normal")
            self.ipaddress2_entry.configure(state="normal")
            self.ipaddress3_entry.configure(state="normal")
            self.ipaddress4_entry.configure(state="normal")

            self.dynaflow_path_entry.configure(state="disabled")
            self.map_path_entry.configure(state="disabled")
            self.dynaflow_path_pull.configure(state="disabled")
            self.dynaflow_path_push.configure(state="disabled")
            self.dynaflow_image_pull.configure(state="disabled")
            self.dynaflow_image_push.configure(state="disabled")
            self.map_path_pull.configure(state="disabled")
            self.open_dynaflow_button.configure(state='disabled')
            # self.map_path_push.configure(state="disabled")

    def limit_spaces(self,entry_text):
        if len(entry_text.get()) > 0:
            entry_texter = "".join([i for i in entry_text.get() if str(i).isdigit()])[:3]
            entry_text.set(entry_texter)

    def pull_files_folder(self, folder_name:str, device_pathing:str, local_pather:str):
        ls_dynaflow = str(self.pepper.shell(f'ls "{self.android_path}/{folder_name}"'))
        filegrab = [i for i in ls_dynaflow.split("\r\n") if "." in i]
        print(filegrab)
        for file in filegrab:
            if device_pathing.endswith("/"):
                device_pather = device_pathing + f"{file}"
            else:
                device_pather = device_pathing + f"/{file}"

            print(device_pather)
            if not os.path.exists(local_pather):
                os.mkdir(local_pather)
            self.pepper.pull(device_path=device_pather, local_path=os.path.join(local_pather, file), progress_callback=self.set_progress)

    def push_files_folder(self, device_pathing:str, local_pather:str):
        filepush = list(os.listdir(local_pather))
        for file in  filepush:
            if device_pathing.endswith("/"):
                device_pather = device_pathing + f"{file}"
            else:
                device_pather = device_pathing + f"/{file}"
            self.pepper.push(device_path=device_pather, local_path=os.path.join(local_pather, file), progress_callback=self.set_progress)

    def pull_files(self, device_pather:str, local_pather:str):
        self.pepper.pull(device_path=device_pather, local_path=local_pather, progress_callback=self.set_progress)

    def get_dynaflow(self):
        self.connect_button.configure(state="disabled")
        self.dynaflow_path_pull.configure(state="disabled")
        self.dynaflow_path_push.configure(state="disabled")

        tt = self.ask_confirmation()
        if tt:
            # grab files
            self.pull_files(device_pather=self.dynaflow_path_var.get(), local_pather=self.local_dyna_json)

        self.dynaflow_path_pull.configure(state="normal")
        self.dynaflow_path_push.configure(state="normal")
        self.connect_button.configure(state="normal")
        self.adb_label.configure(text="Ready")

    def send_dynaflow(self):
        if os.path.exists(self.local_dyna_json):
            self.connect_button.configure(state="disabled")
            self.dynaflow_path_pull.configure(state="disabled")
            self.dynaflow_path_push.configure(state="disabled")
            tt = self.ask_confirmation()
            if tt:
                self.pepper.push(device_path=self.dynaflow_path_var.get(), local_path=self.local_dyna_json, progress_callback=self.set_progress)
            self.dynaflow_path_pull.configure(state="normal")
            self.dynaflow_path_push.configure(state="normal")
            self.connect_button.configure(state="normal")
            self.adb_label.configure(text="Ready")

    def get_dynaflow_images(self):
        self.connect_button.configure(state="disabled")
        self.dynaflow_image_pull.configure(state="disabled")
        self.dynaflow_image_push.configure(state="disabled")
        # grab files
        tt = self.ask_confirmation()
        if tt:
            self.pull_files_folder(folder_name="DynaFlow/images/", device_pathing=self.dynaflow_image_var.get(), local_pather=self.local_dyna_images)
        self.dynaflow_image_pull.configure(state="normal")
        self.dynaflow_image_push.configure(state="normal")
        self.connect_button.configure(state="normal")
        self.adb_label.configure(text="Ready")

    def send_dynaflow_images(self):
        if os.path.exists(self.local_dyna_images):
            self.connect_button.configure(state="disabled")
            self.dynaflow_image_pull.configure(state="disabled")
            self.dynaflow_image_push.configure(state="disabled")
            tt = self.ask_confirmation()
            if tt:
                self.push_files_folder(device_pathing=self.dynaflow_image_var.get(), local_pather=self.local_dyna_images)
            self.dynaflow_image_pull.configure(state="normal")
            self.dynaflow_image_push.configure(state="normal")
            self.connect_button.configure(state="normal")
            self.adb_label.configure(text="Ready")

    def get_map(self):
        self.connect_button.configure(state="disabled")
        self.map_path_pull.configure(state="disabled")
        # self.map_path_push.configure(state="disabled")
        tt = self.ask_confirmation()
        if tt:
            self.pull_files(device_pather=self.map_path_var.get(), local_pather=self.local_map)
        self.map_path_pull.configure(state="normal")
        # self.map_path_push.configure(state="normal")
        self.connect_button.configure(state="normal")
        self.adb_label.configure(text="Ready")
    
    # def send_map(self):
    #     if os.path.exists(self.local_map):
    #         self.connect_button.configure(state="disabled")
    #         self.map_path_pull.configure(state="disabled")
    #         self.map_path_push.configure(state="disabled")
    #         self.pepper.push(device_path=self.map_path_var.get(), local_path=self.local_map, progress_callback=self.set_progress)
    #         self.map_path_pull.configure(state="normal")
    #         self.map_path_push.configure(state="normal")
    #         self.connect_button.configure(state="normal")
    #         self.adb_label.configure(text="Ready")

    def set_progress(self, device_path, bytes_written, total_bytes):
        self.progresser.set(bytes_written/total_bytes)
        self.adb_label.configure(text=f"Progress: [{bytes_written}/{total_bytes}]")

    def open_dynaflow(self):
        if self.pepper != None:
            self.pepper.shell(f"am start -n {package_name}/{package_name}.MainActivity")

    def ask_confirmation(self):
        tf = askyesno("Confirmation", "This action may replace files. Proceed?")
        return