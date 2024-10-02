import javax.crypto.Cipher;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Base64;
import java.util.Scanner;

public class UserDataEncryptionApp {

   // Generate RSA key pair
   public static KeyPair generateRSAKeyPair() throws Exception {
      KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
      keyGen.initialize(2048); // Strong security with 2048-bit key size
      return keyGen.generateKeyPair();
   }

   // Encrypt data (PAN, Aadhar, Mobile) using RSA public key
   public static String encryptData(String data, PublicKey publicKey) throws Exception {
      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.ENCRYPT_MODE, publicKey);
      byte[] encryptedBytes = cipher.doFinal(data.getBytes());
      return Base64.getEncoder().encodeToString(encryptedBytes); // Convert to Base64 for easy storage
   }

   // Decrypt data using RSA private key
   public static String decryptData(String encryptedData, PrivateKey privateKey) throws Exception {
      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.DECRYPT_MODE, privateKey);
      byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
      return new String(decryptedBytes); // Convert bytes back to string
   }

   // Store encrypted data in the database
   public static void storeEncryptedDataInDatabase(String encryptedPan, String encryptedAadhar, String encryptedMobile) throws Exception {
      // Database connection parameters
      String url = "jdbc:mysql://localhost:3306/encryption_db"; // Use your database URL
      String username = "root"; // MySQL username
      String password = "prabhupada100*"; // MySQL password

      // Establish connection
      Connection conn = DriverManager.getConnection(url, username, password);

      // SQL query to insert encrypted data into the table
      String sql = "INSERT INTO user_data (encrypted_pan, encrypted_aadhar, encrypted_mobile) VALUES (?, ?, ?)";
      PreparedStatement statement = conn.prepareStatement(sql);

      // Set the values in the query
      statement.setString(1, encryptedPan);
      statement.setString(2, encryptedAadhar);
      statement.setString(3, encryptedMobile);

      // Execute the query
      int rowsInserted = statement.executeUpdate();
      if (rowsInserted > 0) {
         System.out.println("Data has been successfully inserted into the database.");
      }

      // Close the connection
      conn.close();
   }

   public static void main(String[] args) {
      try {
         // Generate RSA Key Pair
         KeyPair keyPair = generateRSAKeyPair();
         PublicKey publicKey = keyPair.getPublic();
         PrivateKey privateKey = keyPair.getPrivate();

         // Create a Scanner to capture user input for PAN, Aadhar, Mobile Number
         Scanner scanner = new Scanner(System.in);

         // Input: User enters their PAN number
         System.out.print("Enter your PAN number: ");
         String panNumber = scanner.nextLine();

         // Input: User enters their Aadhar number
         System.out.print("Enter your Aadhar number: ");
         String aadharNumber = scanner.nextLine();

         // Input: User enters their Mobile number
         System.out.print("Enter your Mobile number: ");
         String mobileNumber = scanner.nextLine();

         // Encrypt each data input
         String encryptedPan = encryptData(panNumber, publicKey);
         String encryptedAadhar = encryptData(aadharNumber, publicKey);
         String encryptedMobile = encryptData(mobileNumber, publicKey);

         // Store encrypted data in the database
         storeEncryptedDataInDatabase(encryptedPan, encryptedAadhar, encryptedMobile);

         // Decrypt data back to original form
         String decryptedPan = decryptData(encryptedPan, privateKey);
         String decryptedAadhar = decryptData(encryptedAadhar, privateKey);
         String decryptedMobile = decryptData(encryptedMobile, privateKey);

         // Display decrypted data
         System.out.println("Decrypted PAN: " + decryptedPan);
         System.out.println("Decrypted Aadhar: " + decryptedAadhar);
         System.out.println("Decrypted Mobile: " + decryptedMobile);

      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}