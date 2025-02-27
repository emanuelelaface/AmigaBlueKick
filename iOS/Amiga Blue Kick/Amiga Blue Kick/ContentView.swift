import SwiftUI

// Model for managing button names with persistent storage via UserDefaults
class ButtonNamesModel: ObservableObject {
    @Published var names: [String] {
        didSet {
            // Save new names whenever they are modified
            UserDefaults.standard.set(names, forKey: "ButtonNames")
        }
    }
    
    init() {
        // Load saved names if available, otherwise use default values
        if let saved = UserDefaults.standard.array(forKey: "ButtonNames") as? [String] {
            names = saved
        } else {
            names = ["Kickstart 1", "Kickstart 2", "Kickstart 3", "Kickstart 4"]
        }
    }
}

// Vista delle impostazioni per modificare i nomi dei bottoni
struct SettingsView: View {
    @Environment(\.dismiss) var dismiss
    @Binding var names: [String]
    
    // Variabile locale per modificare i testi
    @State private var localNames: [String] = []
    
    var body: some View {
        NavigationView {
            Form {
                // Usa la copia locale per migliorare le performance durante l'editing
                ForEach(localNames.indices, id: \.self) { index in
                    TextField("Name \(index + 1)", text: $localNames[index])
                }
            }
            .navigationTitle("Settings")
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done") {
                        // Aggiorna il modello con i nuovi nomi
                        names = localNames
                        dismiss()
                    }
                }
            }
        }
        .onAppear {
            // Inizializza la copia locale
            self.localNames = names
        }
    }
}

struct ContentView: View {
    @ObservedObject var bleManager = BLEManager()
    @StateObject var buttonNamesModel = ButtonNamesModel() // Use this model for button names
    @State private var showSettings = false
    
    // AmigaOS style palette
    private let amigaBackgroundColor = Color(red: 192/255, green: 192/255, blue: 192/255) // Workbench light gray
    private let amigaButtonColor = Color(red: 128/255, green: 128/255, blue: 128/255)     // Dark gray
    private let amigaSelectedButtonColor = Color(red: 0/255, green: 128/255, blue: 255/255) // Bright blue
    private let amigaTextColor = Color.black
    private let amigaSelectedTextColor = Color.white
    
    var body: some View {
        if bleManager.isSwitchedOn {
            // Determine if we're in the "Searching Amiga Blue Kick" state, in which buttons are disabled.
            let isSearching = (bleManager.status == "Searching Amiga Blue Kick")
            
            ZStack {
                // Background in AmigaOS style
                amigaBackgroundColor
                    .ignoresSafeArea()
                
                VStack(spacing: 20) {
                    Image("AmigaLogo")
                        .resizable()
                        .scaledToFit()
                    Spacer()
                    // Button 1
                    Button(action: {
                        bleManager.send(message: "0")
                    }) {
                        Text(buttonNamesModel.names[0])
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(bleManager.status == "48" ? amigaSelectedButtonColor : amigaButtonColor)
                            .foregroundColor(bleManager.status == "48" ? amigaSelectedTextColor : amigaTextColor)
                            .cornerRadius(30)
                    }
                    
                    // Button 2
                    Button(action: {
                        bleManager.send(message: "1")
                    }) {
                        Text(buttonNamesModel.names[1])
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(bleManager.status == "49" ? amigaSelectedButtonColor : amigaButtonColor)
                            .foregroundColor(bleManager.status == "49" ? amigaSelectedTextColor : amigaTextColor)
                            .cornerRadius(30)
                    }
                    
                    // Button 3
                    Button(action: {
                        bleManager.send(message: "2")
                    }) {
                        Text(buttonNamesModel.names[2])
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(bleManager.status == "50" ? amigaSelectedButtonColor : amigaButtonColor)
                            .foregroundColor(bleManager.status == "50" ? amigaSelectedTextColor : amigaTextColor)
                            .cornerRadius(30)
                    }
                    
                    // Button 4
                    Button(action: {
                        bleManager.send(message: "3")
                    }) {
                        Text(buttonNamesModel.names[3])
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(bleManager.status == "51" ? amigaSelectedButtonColor : amigaButtonColor)
                            .foregroundColor(bleManager.status == "51" ? amigaSelectedTextColor : amigaTextColor)
                            .cornerRadius(30)
                    }
                    
                    Spacer()
                }
                // Disable buttons if we're searching for the device
                .disabled(isSearching)
                .padding()
                .onAppear {
                    bleManager.startScanning()
                }
                
                // Overlay da mostrare quando lo stato è "Searching Amiga Blue Kick"
                if isSearching {
                    VStack {
                        Spacer()
                        Image("WB")
                        Spacer()
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(amigaBackgroundColor)
                    .edgesIgnoringSafeArea(.all)
                }
                
                // Icona dell'ingranaggio delle impostazioni, mostrata solo se non in "Searching"
                if !isSearching {
                    VStack {
                        Spacer()
                        HStack {
                            Spacer()
                            Button(action: {
                                showSettings.toggle()
                            }) {
                                Text("⚙️")
                                    .font(.system(size: 30))
                                    .padding()
                            }
                        }
                    }
                }
                
                // Version information at the bottom
                VStack {
                    Spacer()
                    Text("v\(Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "") developed by Emanuele Laface")
                        .font(.footnote)
                        .foregroundColor(.gray)
                        .multilineTextAlignment(.center)
                        .padding(.bottom, 8)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottom)
            }
            // Presenta la SettingsView in un foglio, passando il binding ai nomi del modello
            .sheet(isPresented: $showSettings) {
                SettingsView(names: $buttonNamesModel.names)
            }
        } else {
            Text("Bluetooth is NOT switched on")
                .foregroundColor(.red)
        }
    }
}
