import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(
            onGetLocation: { callback in
                LocationHelper.shared.getCurrentLocation { lat, lng in
                    callback(KotlinDouble(value: lat), KotlinDouble(value: lng))
                }
            }
        )
    }

    func updateUIViewController(
        _ uiViewController: UIViewController,
        context: Context
    ) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.all, edges: .all)
    }
}