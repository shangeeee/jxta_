/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.groupJoin.security;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import net.jxta.document.AdvertisementFactory;
import net.jxta.impl.membership.pse.FileKeyStoreManager;
import net.jxta.impl.membership.pse.PSEUtils;
import net.jxta.impl.protocol.PSEConfigAdv;
import net.jxta.peer.PeerID;

/**
 *
 * @author The Boss
 */
public class LocalKeyStore {
    private final String keyStoreProvider = "Four4";
    private final String keyStoreLocation = "." + System.getProperty("file.separator") + ".jxta" + File.separator + "keyStoreLocation";
    private final String keyStoreFileName = "keyStore";
    private File keyStoreDir;
    private File keyStoreFile;
    
    private String privateKeyPassword;
    private String keyStorePassword ;
    
    private X509Certificate theX509Certificate;
    private PrivateKey thePrivateKey;
    
    private FileKeyStoreManager keyStoreManager;
    
    private PeerID peerID;

    private LocalKeyStore() {
        keyStoreDir = new File(keyStoreLocation);
        keyStoreFile = new File(keyStoreLocation + File.separator + keyStoreFileName);
        if(!keyStoreDir.exists()){
            keyStoreDir.mkdirs();
        }
        if(keyStoreFile.exists()){
            keyStoreFile.delete();
        }
    }
    
    

    public LocalKeyStore(PeerID peerID , String peerName , String keyStorePassword ,String privateKeyPassword) throws NoSuchProviderException, KeyStoreException, IOException{
        this();
        this.privateKeyPassword = privateKeyPassword;
        this.keyStorePassword = keyStorePassword;
        this.peerID = peerID;
        keyStoreFile = new File(keyStoreLocation + File.separator + peerName +"_"+ keyStoreFileName);
        
        PSEUtils.IssuerInfo forPSE = PSEUtils.genCert(peerName, null);
        theX509Certificate = forPSE.cert;
        thePrivateKey = forPSE.issuerPkey;
        
        keyStoreManager = new FileKeyStoreManager((String)null, keyStoreProvider, keyStoreFile);
        
        keyStoreManager.createKeyStore(keyStorePassword.toCharArray());
        
        if(keyStoreManager.isInitialized(keyStorePassword.toCharArray())){
            System.out.println("Key store initialized ");
            
           
        }
        else{
            System.out.println("Key store not initialized");
        }
        
        //Loading the keystore
        KeyStore keyStore = keyStoreManager.loadKeyStore(keyStorePassword.toCharArray());
        //Setting datas
        X509Certificate[] temp = { theX509Certificate };
        keyStore.setKeyEntry(peerID.toString(), thePrivateKey, this.privateKeyPassword.toCharArray(),temp);
        
        //Saving datas
        keyStoreManager.saveKeyStore(keyStore, this.keyStorePassword.toCharArray());
        
        
        // Retrieving Certificate
            X509Certificate myCertificate = (X509Certificate) keyStore.getCertificate(peerID.toString());
           
    }

    public String getPrivateKeyPassword() {
        return privateKeyPassword;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }
    
    
    public URI getFileLocation(){
        return keyStoreFile.toURI();
    }

    public PeerID getPeerID() {
        return peerID;
    }
    
    public PSEConfigAdv getPSEConfigAdv(){
       PSEConfigAdv pseca = (PSEConfigAdv) AdvertisementFactory.newAdvertisement(PSEConfigAdv.getAdvertisementType());
       return pseca;
    }
    
   
}
 