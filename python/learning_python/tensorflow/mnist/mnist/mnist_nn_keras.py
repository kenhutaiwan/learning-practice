import numpy as np
from keras.datasets import mnist
from keras.utils import np_utils
from keras.models import Sequential
from keras.layers import Dense

np.random.seed(1337)
batch_size = 128
nb_classes = 10
nb_epoch = 10
img_size = 28 * 28

(X_train, y_train), (X_test, y_test) = mnist.load_data()
X_train = X_train.reshape(y_train.shape[0], img_size).astype('float32') / 255
X_test = X_test.reshape(y_test.shape[0], img_size).astype('float32') / 255

print(X_train.shape, X_test.shape)

y_train = np_utils.to_categorical(y_train, nb_classes)
y_test = np_utils.to_categorical(y_test, nb_classes)

model = Sequential([Dense(10, input_shape=(img_size,), activation='softmax'),])
model.compile(optimizer='rmsprop', loss='categorical_crossentropy', metrics=['accuracy'])
model.fit(X_train, y_train, batch_size=batch_size, nb_epoch=nb_epoch, verbose=1,validation_data=(X_test, y_test))
