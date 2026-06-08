import Foundation
import CoreLocation

class LocationHelper: NSObject, CLLocationManagerDelegate {

    static let shared = LocationHelper()

    private let manager = CLLocationManager()
    private var callback: ((Double, Double) -> Void)?

    override init() {
        super.init()
        manager.delegate        = self
        manager.desiredAccuracy = kCLLocationAccuracyBest
    }

    func getCurrentLocation(completion: @escaping (Double, Double) -> Void) {
        callback = completion
        switch manager.authorizationStatus {
        case .notDetermined:
            manager.requestWhenInUseAuthorization()
        case .authorizedWhenInUse, .authorizedAlways:
            manager.requestLocation()
        case .denied, .restricted:
            // Sin permiso — no hace nada
            callback = nil
        @unknown default:
            callback = nil
        }
    }

    // ── CLLocationManagerDelegate ─────────────────────────────────────

    func locationManager(
        _ manager: CLLocationManager,
        didUpdateLocations locations: [CLLocation]
    ) {
        guard let location = locations.last else { return }
        callback?(location.coordinate.latitude, location.coordinate.longitude)
        callback = nil
    }

    func locationManager(
        _ manager: CLLocationManager,
        didFailWithError error: Error
    ) {
        print("[LocationHelper] Error: \(error.localizedDescription)")
        callback = nil
    }

    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        switch manager.authorizationStatus {
        case .authorizedWhenInUse, .authorizedAlways:
            manager.requestLocation()
        default:
            break
        }
    }
}