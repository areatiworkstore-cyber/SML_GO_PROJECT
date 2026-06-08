import SwiftUI
import Shared

@main
struct iOSApp: App {

    init() {
        // Inicializa Koin para iOS
        KoinHelperKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .ignoresSafeArea(.all)
        }
    }
}