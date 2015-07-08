## Image Processing & Classification Webservice Template
A Play Framework webservice that analyses images and classifies the resulting feature vectors.
The image processing is done using OpenCV (http://opencv.org/). For the classification the service uses the data mining framework Weka (http://www.cs.waikato.ac.nz/ml/weka/).
The process is executed using akka. The resulting feature vectors and classes are stored in mongoDB (https://www.mongodb.org/) where image data is stored with GridFS.

The project can be used as a basis for webservice based image processing and classification. The example data provided was created using images of butterflies. The training set is neither optimised nor normalised. The provided image processing plugins are only a rudimentary stub implementation to illustrate the usage of this template. The butterfly classification data was created in a scientific context in association with a butterfly exhibitor. The technical background regarding the plugin system is based on the work of the authors Benjamin Müller and Thomas Rawyler (http://meteor-ai.sourceforge.net/).
To start using this webservice you need a training set of manually classified images and of course your algorithms to perform the image processing on the images. The algorithms have to be implemented as plugins as seen in the presented example. That's of course the hard work in the field of image processing and artificial intelligence.
The project comes with a training set service which can be used to generate the corresponding Weka arff file based on your plugin implementations and training set images.
The classifier used by Weka can be set in the application.conf with the property classifier.default.

Side note: because of the native library integration the app has to be started using sbt start (https://github.com/playframework/playframework/issues/2212).
After starting the application you can navigate to http://localhost:9000 where you'll find a simple upload form for submitting images for analysis and classification (top 3 classes for the given input).
A client application would typically perform a POST request to /identifications receiving the stub identification with its id and image upload url. After that an image can be uploaded using this url with method POST and multipart/form-data containing the file.

## License
Copyright 2015 Tegonal GmbH http://tegonal.com
http://www.apache.org/licenses/LICENSE-2.0
