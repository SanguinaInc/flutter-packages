import 'package:camera_platform_interface/camera_platform_interface.dart';

class AndroidCameraDescription extends CameraDescription {
  AndroidCameraDescription({
    required super.name,
    required super.lensDirection,
    required super.sensorOrientation,
    this.physicalCameras,
  }) {
  }

  final List<PhysicalCameraDescription>? physicalCameras;
  String? selectedPhysicalCamera;

  @override
  String toString() {
    return 'CameraDescription($name, $lensDirection, $sensorOrientation, ${physicalCameras})';
  }
}

class PhysicalCameraDescription {
  PhysicalCameraDescription({
    required this.id,
    required this.minZoom,
    required this.maxZoom,
    required this.aperture,
  }) {}

  String id;
  double? minZoom;
  double? maxZoom;
  double? aperture;

  @override
  String toString() {
    return 'PhysicalCameraDescription($id, $minZoom, $maxZoom, $aperture)';
  }
}
