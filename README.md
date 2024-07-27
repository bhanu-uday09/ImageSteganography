# Image Steganography
Steganography is the process of hiding a secret message within a larger one in such a way that someone cannot know the presence or contents of the hidden message. Although related, steganography is not to be confused with encryption, which is the process of making a message unintelligible. Steganography attempts to hide the existence of communication.

The main advantage of steganography is its simple security mechanism. Because the steganographic message is invisibly integrated and covered inside other harmless sources, it is very difficult to detect the message without knowing its existence and the appropriate encoding scheme.

# Proposed Algorithm
The algorithm is inspired by Rosziati Ibrahim and Teoh Suk Kuan in their [research paper](https://arxiv.org/ftp/arxiv/papers/1112/1112.2809.pdf) published on February 25, 2011.

## Encoding Algorithm
1. **Compress** the secret message. The contents in the compressed string are significantly harder to detect and read, and this reduces the size of the string.
2. **Encrypt** the compressed string with the secret key.
3. **Encode** the encrypted message in the image using LSB steganographic embedding. This process stops once the message is encoded.

### LSB (Least Significant Bit) Embedding
The LSB is the lowest significant bit in the byte value of the image pixel. LSB-based image steganography embeds the secret in the least significant bits of pixel values of the cover image (CVR). This technique exploits the fact that the level of precision in many image formats is far greater than that perceivable by average human vision. An altered image with slight variations in its colors will be indistinguishable from the original to a human being. In the proposed LSB technique, four bytes of pixels are sufficient to hold one message byte, unlike the conventional technique which requires eight bytes of pixels to store one byte of secret data.

![image](https://github.com/user-attachments/assets/607bf547-3068-41e9-9792-b93a0ecabf38)


## Decoding Algorithm
1. **Decode** the message from the encrypted image using LSB decoding.
2. **Decrypt** the compressed message from the decoded message using the secret key.
3. **Decompress** the message to get the original compressed message.

Consider the following encoding; it is totally undetectable by human eyes.

![image](https://github.com/user-attachments/assets/6fe96898-b9a1-4c75-af4c-009401ee43e3)


# Documentation

### ImageSteganography Class

| Attribute          | Set Methods                  | Description                                                  | Default Value |
| :----------------- | :--------------------------- | :----------------------------------------------------------- | :-----------: |
| Message            | setMessage(...), getMessage()| Set the value of message, get the value of message.           | Null          |
| Secret Key         | setSecretKey(...)            | Set the value of secret key.                                  | Null          |
| Image              | setImage(...)                | Set the value of image.                                       | Null          |
| Encoded Image      | getEncodedImage()            | Get the value of encoded image after text encoding.           | Null          |
| Encoded            | isEncoded()                  | Check that the encoding is over or not.                       | false         |
| Decoded            | isDecoded()                  | Check that the decoding is over or not.                       | false         |
| Secret Key Wrong   | isSecretKeyWrong()           | Check that the secret key provided was right or wrong after decoding was done. | true          |

### Class Domain Diagram

![image](https://github.com/user-attachments/assets/ffbe249b-bcc3-4e89-9fe1-aad1d57b2c5c)


