# AutoDriveVision

![AutoDriveVision Logo](autodrive_logo.png)

**AutoDriveVision** is an open-source Android application designed for real-time object recognition in autonomous driving scenarios. Using machine learning, it detects and classifies key objects such as vehicles, pedestrians, traffic signs, and lane markings directly on your Android device using the camera. The project is lightweight, optimized for mobile performance, and works offline.

## Features
- Real-time object detection via Android camera
- Recognizes driving-related objects:
  - Vehicles
  - Pedestrians
  - Traffic signs
  - Lane markings
- Built with TensorFlow Lite for efficient mobile inference
- Modern UI with Jetpack Compose
- Fully open-source under the MIT License

## Demo
*Coming soon!*

## Prerequisites
- Android Studio (2023.1.1 or later)
- Android device or emulator running API 21 (Android 5.0) or higher
- Basic knowledge of Kotlin and Android development

## Installation

1. **Clone the Repository**
   ```bash
   git clone https://github.com/techdrivex/AutoDriveVision.git
   cd AutoDriveVision
   ```

2. **Open in Android Studio**
   - Launch Android Studio.
   - Select "Open an existing project" and choose the `AutoDriveVision` folder.

3. **Sync Dependencies**
   - Ensure you have an internet connection.
   - Click "Sync Project with Gradle Files" in Android Studio to download dependencies.

4. **Add the Model**
   - Place a pre-trained TensorFlow Lite model (e.g., `model.tflite`) in the `app/src/main/assets/` folder.  
     *(A sample model will be provided soon, or see "Training Your Own Model" below.)*

5. **Run the App**
   - Connect an Android device via USB or start an emulator.
   - Click "Run" in Android Studio to build and install the app.

## Usage
1. Launch the app on your Android device.
2. Grant camera permissions when prompted.
3. Point the camera at a scene (e.g., a road with vehicles or signs).
4. The app will display bounding boxes and labels around detected objects in real-time.

## Project Structure
```
AutoDriveVision/
├── app/                    # Main Android app module
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/autodrivevision/  # Core logic
│   │   │   ├── res/                     # Resources
│   │   │   └── assets/                  # ML model files
│   └── build.gradle                    # App-level build config
├── docs/                       # Documentation and assets
├── LICENSE                    # MIT License
└── README.md                 # This file
```

## Training Your Own Model
To customize the object detection model:
1. Use a dataset like [COCO](https://cocodataset.org/), [KITTI](http://www.cvlibs.net/datasets/kitti/), or your own labeled images.
2. Train a lightweight model (e.g., MobileNet SSD or YOLOv5s) using TensorFlow or PyTorch.
3. Convert the model to TensorFlow Lite format:
   ```bash
   tflite_convert --graph_def_file=model.pb --output_file=model.tflite --input_arrays=input --output_arrays=output
   ```
4. Place the `.tflite` file in `app/src/main/assets/`.

## Contributing
We welcome contributions! Here's how to get involved:
1. Fork the repository.
2. Create a new branch (`git checkout -b feature/your-feature`).
3. Make your changes and commit (`git commit -m "Add your feature"`).
4. Push to your fork (`git push origin feature/your-feature`).
5. Open a Pull Request.

Please follow these guidelines:
- Write clear, concise commit messages.
- Test your changes on at least one Android device/emulator.
- Update documentation if necessary.

## Issues
Found a bug or have a feature request? Open an issue [here](https://github.com/techdrivex/AutoDriveVision/issues).

## Roadmap
- Add pre-trained model to the repo
- Improve detection accuracy with custom datasets
- Support additional object classes (e.g., traffic lights)
- Optimize for lower-end devices

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments
- Built with [TensorFlow Lite](https://www.tensorflow.org/lite)
- Inspired by open-source autonomous driving projects
- Thanks to the Android developer community
