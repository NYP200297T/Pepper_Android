import customtkinter
import os
from tkinter.messagebox import askyesno, showwarning
from ppadb.client import Client as AdbClient

package_name = "org.ishoot.dynaflow"

class AdbFrame(customtkinter.CTkFrame):

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)

        self.pepper = None
        # default ADB server
        self.client = AdbClient(host="localhost", port=5037)

        # initalise pathing variables
        self.local_dyna_json = os.path.join(os.getcwd(), "DynaFlow", "flow.json")
        self.local_dyna_images = os.path.join(os.getcwd(), "DynaFlow", "images")
        self.local_map = os.path.join(os.getcwd(), "Maps")
        self.local_map_json = os.path.join(self.local_map, "points.json")
        if not os.path.exists(os.path.join(os.getcwd(), "DynaFlow")):
            os.mkdir(os.path.join(os.getcwd(), "DynaFlow"))
        
        # first setup of adb
        self.android_path = "storage/emulated/0"

        # info label
        self.adb_label = customtkinter.CTkLabel(self, text="Disconnected", anchor="center")
        self.adb_label.pack(expand=True, anchor='center', fill='both', padx=10, pady=10)

        self.connect_frame = customtkinter.CTkFrame(self)

        # IP Address entry boxes and connect
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
        
        # prevent more than 3 characters, numbers only and 0 ≤ input ≤ 255
        self.ipaddress1.trace("w", lambda *args: self.limit_spaces(self.ipaddress1))
        self.ipaddress2.trace("w", lambda *args: self.limit_spaces(self.ipaddress2))
        self.ipaddress3.trace("w", lambda *args: self.limit_spaces(self.ipaddress3))
        self.ipaddress4.trace("w", lambda *args: self.limit_spaces(self.ipaddress4))

        self.ipaddress_frame.grid(row=0, column=0, sticky="nsew", padx=10, pady=10)

        # connect physical device button
        self.connect_button = customtkinter.CTkSwitch(self.connect_frame, text="Connect", command=self.connect_pepper)
        self.connect_button.grid(row=0, column=1, sticky="nsew", padx=10, pady=10)

        self.connect_frame.pack(expand=True, anchor='center', fill='both', padx=10, pady=10)

        # connect emulator button
        self.connect_emu_button = customtkinter.CTkSwitch(self, text="Connect emulator", command=self.connect_emulator)
        self.connect_emu_button.pack(expand=True, anchor='center', fill='both', padx=10, pady=10)

        # dynaflow flow.json path
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

        self.dynaflow_path_frame.pack(expand=True, anchor='center', fill='both', padx=10, pady=2)

        # dynaflow images folder path
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

        self.dynaflow_image_frame.pack(expand=True, anchor='center', fill='both', padx=10, pady=2)

        # map frame for folder path
        self.map_path_frame = customtkinter.CTkFrame(self)
        self.map_path_frame.grid_rowconfigure(0, weight=1)
        self.map_path_frame.grid_columnconfigure(0, weight=1)
        
        self.map_path_var = customtkinter.StringVar(value=self.android_path+"/Maps/points.json")
        self.map_path_entry = customtkinter.CTkEntry(self.map_path_frame, textvariable=self.map_path_var)
        self.map_path_entry.grid(row=0, column=0, sticky="nsew", padx=5, pady=5)
        self.map_path_pull = customtkinter.CTkButton(self.map_path_frame, text="Pull", width=50, command=self.get_map)
        self.map_path_pull.grid(row=0, column=1, padx=5, columnspan=2, pady=5)
        # self.map_path_push = customtkinter.CTkButton(self.map_path_frame, text="Push", width=50, command=self.send_map)
        # self.map_path_push.grid(row=0, column=2, padx=5, pady=5)

        self.map_path_frame.pack(expand=True, anchor='center', fill='both', padx=10, pady=5)

        # open DynaFlow in Pepper
        self.open_dynaflow_button = customtkinter.CTkButton(self, text="Open DynaFlow", command=self.open_dynaflow)
        self.open_dynaflow_button.pack(expand=True, anchor='center', fill='both', padx=10, pady=5)

        # set initial UI element disabled
        self.dynaflow_path_entry.configure(state="disabled")
        self.map_path_entry.configure(state="disabled")
        self.dynaflow_path_pull.configure(state="disabled")
        self.dynaflow_path_push.configure(state="disabled")
        self.dynaflow_image_pull.configure(state="disabled")
        self.dynaflow_image_push.configure(state="disabled")
        self.map_path_pull.configure(state="disabled")
        self.open_dynaflow_button.configure(state='disabled')
        # self.map_path_push.configure(state="disabled")

    # connecting to pepper
    def connect_pepper(self):

        # if switch set to on
        if bool(self.connect_button.get()):
            # check if Pepper has been connected before
            if self.pepper != None:
                self.client.remote_disconnect()
                self.pepper = None

            # grab IP address
            if len(self.ipaddress1.get()) > 0 and len(self.ipaddress2.get()) > 0 and len(self.ipaddress3.get()) > 0 and len(self.ipaddress4.get()) > 0:
                try:
                    connected = self.client.remote_connect(host=f'{self.ipaddress1.get()}.{self.ipaddress2.get()}.{self.ipaddress3.get()}.{self.ipaddress4.get()}', port=5555)
                except Exception as e:
                    showwarning(message="Please run <adb devices> in command prompt first.")
                    connected = False
            else:
                connected = False
                    
            if connected:
                # set device and enable functions
                self.pepper = self.client.device(f'{self.ipaddress1.get()}.{self.ipaddress2.get()}.{self.ipaddress3.get()}.{self.ipaddress4.get()}:5555')

                self.adb_label.configure(text="Connected")
                self.ipaddress1_entry.configure(state="disabled")
                self.ipaddress2_entry.configure(state="disabled")
                self.ipaddress3_entry.configure(state="disabled")
                self.ipaddress4_entry.configure(state="disabled")
                self.connect_emu_button.configure(state="disabled")

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
                self.pepper = None

        # if switch set to off
        else:
            self.client.remote_disconnect()
            self.pepper = None

            self.adb_label.configure(text="Disconnected")
            self.ipaddress1_entry.configure(state="normal")
            self.ipaddress2_entry.configure(state="normal")
            self.ipaddress3_entry.configure(state="normal")
            self.ipaddress4_entry.configure(state="normal")
            self.connect_emu_button.configure(state="normal")

            self.dynaflow_path_entry.configure(state="disabled")
            self.map_path_entry.configure(state="disabled")
            self.dynaflow_path_pull.configure(state="disabled")
            self.dynaflow_path_push.configure(state="disabled")
            self.dynaflow_image_pull.configure(state="disabled")
            self.dynaflow_image_push.configure(state="disabled")
            self.map_path_pull.configure(state="disabled")
            self.open_dynaflow_button.configure(state='disabled')
            # self.map_path_push.configure(state="disabled")

    # connecting to emulator
    def connect_emulator(self):

        # if switch on
        if bool(self.connect_emu_button.get()):

            # search for emulator devices on ADB server
            devices = [i for i in self.client.devices() if str(i.serial).startswith("emulator-")]
            if len(devices) > 0:
                self.pepper = devices[0] # grab device

                self.adb_label.configure(text="Connected")
                self.ipaddress1_entry.configure(state="disabled")
                self.ipaddress2_entry.configure(state="disabled")
                self.ipaddress3_entry.configure(state="disabled")
                self.ipaddress4_entry.configure(state="disabled")
                self.connect_button.configure(state="disabled")

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
                showwarning(message="No emulators detected, please ensure emulator is running")
                self.adb_label.configure(text="Error, not connected")
                self.connect_emu_button.deselect()
                self.pepper = None
                return

        # if switch set to off
        else:
            self.client.remote_disconnect()
            self.pepper = None

            self.adb_label.configure(text="Disconnected")
            self.ipaddress1_entry.configure(state="normal")
            self.ipaddress2_entry.configure(state="normal")
            self.ipaddress3_entry.configure(state="normal")
            self.ipaddress4_entry.configure(state="normal")
            self.connect_button.configure(state="normal")

            self.dynaflow_path_entry.configure(state="disabled")
            self.map_path_entry.configure(state="disabled")
            self.dynaflow_path_pull.configure(state="disabled")
            self.dynaflow_path_push.configure(state="disabled")
            self.dynaflow_image_pull.configure(state="disabled")
            self.dynaflow_image_push.configure(state="disabled")
            self.map_path_pull.configure(state="disabled")
            self.open_dynaflow_button.configure(state='disabled')
            # self.map_path_push.configure(state="disabled")

    # limit spaces for IP address to only numbers, 3 chars long and ≤255
    def limit_spaces(self,entry_text):
        if len(entry_text.get()) > 0:
            entry_texter = "".join([i for i in entry_text.get() if str(i).isdigit()])[:3]
            entry_texter = str(int(entry_texter)) # remove leading zero
            if int(entry_texter) > 255:
                entry_texter = "255"
            entry_text.set(entry_texter)

    # pull files from a folder in Pepper
    def pull_files_folder(self, folder_name, device_pathing, local_pather, ignore:list=[]):
        # check files in folder in Pepper
        ls_dynaflow = str(self.pepper.shell(f'ls "{self.android_path}/{folder_name}"'))
        # checks for files only
        filegrab = [i for i in ls_dynaflow.split("\r\n") if "." in i]
        for file in filegrab:
            if file in ignore:
                continue
            # fix pathing
            if device_pathing.endswith("/"):
                device_pather = device_pathing + f"{file}"
            else:
                device_pather = device_pathing + f"/{file}"
            # add folder in case doesn't exist
            if not os.path.exists(local_pather):
                os.mkdir(local_pather)
            # pull file from Pepper
            self.pull_file(device_path=device_pather, local_path=os.path.join(local_pather, file))

    # push files from on device to Pepper
    def push_files_folder(self, device_pathing, local_pather):
        filepush = list(os.listdir(local_pather))
        for file in filepush:
            if device_pathing.endswith("/"):
                device_pather = device_pathing + f"{file}"
            else:
                device_pather = device_pathing + f"/{file}"
            self.push_file(device_path=device_pather, local_path=os.path.join(local_pather, file))

    # pull single file from Pepper
    def pull_file(self, device_path, local_path):
       
        try:
            self.pepper.pull(device_path, local_path)
        # if Pepper is disconnected
        except RuntimeError:
            # turn off both connection switches
            if bool(self.connect_emu_button.get()):
                self.connect_emu_button.deselect()
                self.connect_emulator()
            if bool(self.connect_button.get()):
                self.connect_button.deselect()
                self.connect_pepper()
            showwarning(message="Error: Device disconnected")

    # push single file from Pepper
    def push_file(self, device_path, local_path):
        try:
            self.pepper.push(local_path, device_path)
        # if Pepper is disconnected
        except RuntimeError:
            # turn off both connection switches
            if bool(self.connect_emu_button.get()):
                self.connect_emu_button.deselect()
                self.connect_emulator()
            if bool(self.connect_button.get()):
                self.connect_button.deselect()
                self.connect_pepper()
            showwarning(message="Error: Device disconnected")

    # get DynaFlow flow.json from Pepper to computer
    def get_dynaflow(self):
        # disabled connections
        self.connect_button.configure(state="disabled")
        self.dynaflow_path_pull.configure(state="disabled")
        self.dynaflow_path_push.configure(state="disabled")

        # ask for confirmation due to sensitive action
        tt = self.ask_confirmation()
        if tt:
            self.pull_file(device_path=self.dynaflow_path_var.get(), local_path=self.local_dyna_json)

        # re-enable connections
        self.dynaflow_path_pull.configure(state="normal")
        self.dynaflow_path_push.configure(state="normal")
        self.connect_button.configure(state="normal")
        self.adb_label.configure(text="Ready")

    # send DynaFlow flow.json to Pepper
    def send_dynaflow(self):
        if os.path.exists(self.local_dyna_json):
            # disabled connections
            self.connect_button.configure(state="disabled")
            self.dynaflow_path_pull.configure(state="disabled")
            self.dynaflow_path_push.configure(state="disabled")
            
            # ask for confirmation due to sensitive action
            tt = self.ask_confirmation()
            if tt:
                self.push_file(device_path=self.dynaflow_path_var.get(), local_path=self.local_dyna_json)
                
            # re-enable connections
            self.dynaflow_path_pull.configure(state="normal")
            self.dynaflow_path_push.configure(state="normal")
            self.connect_button.configure(state="normal")
            self.adb_label.configure(text="Ready")

    # get DynaFlow images folder from Pepper to computer
    def get_dynaflow_images(self):
        # disabled connections
        self.connect_button.configure(state="disabled")
        self.dynaflow_image_pull.configure(state="disabled")
        self.dynaflow_image_push.configure(state="disabled")
        
        # ask for confirmation due to sensitive action
        tt = self.ask_confirmation()
        if tt:
            self.pull_files_folder(folder_name="DynaFlow/images/", device_pathing=self.dynaflow_image_var.get(), local_pather=self.local_dyna_images)
        
        # re-enable connections
        self.dynaflow_image_pull.configure(state="normal")
        self.dynaflow_image_push.configure(state="normal")
        self.connect_button.configure(state="normal")
        self.adb_label.configure(text="Ready")

    # send DynaFlow images folder from computer to Pepper
    def send_dynaflow_images(self):
        if os.path.exists(self.local_dyna_images):
            # disabled connections
            self.connect_button.configure(state="disabled")
            self.dynaflow_image_pull.configure(state="disabled")
            self.dynaflow_image_push.configure(state="disabled")
            
            # ask for confirmation due to sensitive action
            tt = self.ask_confirmation()
            if tt:
                self.push_files_folder(device_pathing=self.dynaflow_image_var.get(), local_pather=self.local_dyna_images)
            
            # re-enabled connections
            self.dynaflow_image_pull.configure(state="normal")
            self.dynaflow_image_push.configure(state="normal")
            self.connect_button.configure(state="normal")
            self.adb_label.configure(text="Ready")

    # get /Maps/points.json generated from MapLocaliseAndMove app
    def get_map(self):
        # disabled connections
        self.connect_button.configure(state="disabled")
        self.map_path_pull.configure(state="disabled")
        # self.map_path_push.configure(state="disabled")
        
        # ask for confirmation due to sensitive action
        tt = self.ask_confirmation()
        if tt:
            if not os.path.exists(self.local_map):
                os.mkdir(self.local_map)
            self.pull_file(device_path=self.map_path_var.get(), local_path=self.local_map_json)
        
        # re-enable connections
        self.map_path_pull.configure(state="normal")
        self.connect_button.configure(state="normal")
        self.adb_label.configure(text="Ready")
    
    # removed since points data cannot be edited for now, no point sending points data back into Pepper
    # def send_map(self):
    #     if os.path.exists(self.local_map):
    #         self.connect_button.configure(state="disabled")
    #         self.map_path_pull.configure(state="disabled")
    #         self.map_path_push.configure(state="disabled")
    #         self.push_file(local_path=self.local_map_json, device_path=self.map_path_var.get())
    #         self.map_path_pull.configure(state="normal")
    #         self.map_path_push.configure(state="normal")
    #         self.connect_button.configure(state="normal")
    #         self.adb_label.configure(text="Ready")

    # def set_progress(self, device_path, bytes_written, total_bytes):
    #     self.progresser.set(bytes_written/total_bytes)
    #     self.adb_label.configure(text=f"Progress: [{bytes_written}/{total_bytes}]")

    def open_dynaflow(self):
        if self.pepper != None:
            self.pepper.shell(f"am force-stop {package_name}")
            self.pepper.shell(f"am start -n {package_name}/{package_name}.MainActivity")

    def ask_confirmation(self):
        tf = askyesno("Confirmation", "This action may replace files and will stop DynaFlow. Proceed?")
        return tf

    def destroy(self):
        if self.client != None:
            try:
                self.client.remote_disconnect()
            except RuntimeError:
                pass
        return super().destroy()
