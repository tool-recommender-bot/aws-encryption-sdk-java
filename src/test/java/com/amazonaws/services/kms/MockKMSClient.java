/*
 * Copyright 2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except
 * in compliance with the License. A copy of the License is located at
 * 
 * http://aws.amazon.com/apache2.0
 * 
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.amazonaws.services.kms;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.ResponseMetadata;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kms.model.CreateAliasRequest;
import com.amazonaws.services.kms.model.CreateGrantRequest;
import com.amazonaws.services.kms.model.CreateGrantResult;
import com.amazonaws.services.kms.model.CreateKeyRequest;
import com.amazonaws.services.kms.model.CreateKeyResult;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;
import com.amazonaws.services.kms.model.DeleteAliasRequest;
import com.amazonaws.services.kms.model.DescribeKeyRequest;
import com.amazonaws.services.kms.model.DescribeKeyResult;
import com.amazonaws.services.kms.model.DisableKeyRequest;
import com.amazonaws.services.kms.model.DisableKeyRotationRequest;
import com.amazonaws.services.kms.model.EnableKeyRequest;
import com.amazonaws.services.kms.model.EnableKeyRotationRequest;
import com.amazonaws.services.kms.model.EncryptRequest;
import com.amazonaws.services.kms.model.EncryptResult;
import com.amazonaws.services.kms.model.GenerateDataKeyRequest;
import com.amazonaws.services.kms.model.GenerateDataKeyResult;
import com.amazonaws.services.kms.model.GenerateDataKeyWithoutPlaintextRequest;
import com.amazonaws.services.kms.model.GenerateDataKeyWithoutPlaintextResult;
import com.amazonaws.services.kms.model.GenerateRandomRequest;
import com.amazonaws.services.kms.model.GenerateRandomResult;
import com.amazonaws.services.kms.model.GetKeyPolicyRequest;
import com.amazonaws.services.kms.model.GetKeyPolicyResult;
import com.amazonaws.services.kms.model.GetKeyRotationStatusRequest;
import com.amazonaws.services.kms.model.GetKeyRotationStatusResult;
import com.amazonaws.services.kms.model.InvalidCiphertextException;
import com.amazonaws.services.kms.model.KeyMetadata;
import com.amazonaws.services.kms.model.KeyUsageType;
import com.amazonaws.services.kms.model.ListAliasesRequest;
import com.amazonaws.services.kms.model.ListAliasesResult;
import com.amazonaws.services.kms.model.ListGrantsRequest;
import com.amazonaws.services.kms.model.ListGrantsResult;
import com.amazonaws.services.kms.model.ListKeyPoliciesRequest;
import com.amazonaws.services.kms.model.ListKeyPoliciesResult;
import com.amazonaws.services.kms.model.ListKeysRequest;
import com.amazonaws.services.kms.model.ListKeysResult;
import com.amazonaws.services.kms.model.NotFoundException;
import com.amazonaws.services.kms.model.PutKeyPolicyRequest;
import com.amazonaws.services.kms.model.ReEncryptRequest;
import com.amazonaws.services.kms.model.ReEncryptResult;
import com.amazonaws.services.kms.model.RetireGrantRequest;
import com.amazonaws.services.kms.model.RevokeGrantRequest;
import com.amazonaws.services.kms.model.UpdateKeyDescriptionRequest;

public class MockKMSClient extends AWSKMSClient {
    private static final SecureRandom rnd = new SecureRandom();
    private static final String ACCOUNT_ID = "01234567890";
    private final Map<DecryptMapKey, DecryptResult> results_ = new HashMap<>();
    private final Map<String, String> idToArnMap = new HashMap<>();
    private final Set<String> activeKeys = new HashSet<>();
    private Region region_ = Region.getRegion(Regions.DEFAULT_REGION);

    @Override
    public void createAlias(CreateAliasRequest arg0) throws AmazonServiceException, AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public CreateGrantResult createGrant(CreateGrantRequest arg0) throws AmazonServiceException, AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public CreateKeyResult createKey() throws AmazonServiceException, AmazonClientException {
        return createKey(new CreateKeyRequest());
    }

    @Override
    public CreateKeyResult createKey(CreateKeyRequest req) throws AmazonServiceException, AmazonClientException {
        String keyId = UUID.randomUUID().toString();
        String arn = "arn:aws:kms:" + region_.getName() + ":" + ACCOUNT_ID + ":key/" + keyId;
        idToArnMap.put(keyId, arn);
        activeKeys.add(arn);
        CreateKeyResult result = new CreateKeyResult();
        result.setKeyMetadata(new KeyMetadata().withAWSAccountId(ACCOUNT_ID).withCreationDate(new Date())
                .withDescription(req.getDescription()).withEnabled(true).withKeyId(keyId)
                .withKeyUsage(KeyUsageType.ENCRYPT_DECRYPT).withArn(arn));
        return result;
    }

    @Override
    public DecryptResult decrypt(DecryptRequest req) throws AmazonServiceException, AmazonClientException {
        DecryptResult result = results_.get(new DecryptMapKey(req));
        if (result != null) {
            // Copy it to avoid external modification
            DecryptResult copy = new DecryptResult();
            copy.setKeyId(retrieveArn(result.getKeyId()));
            byte[] pt = new byte[result.getPlaintext().limit()];
            result.getPlaintext().get(pt);
            result.getPlaintext().rewind();
            copy.setPlaintext(ByteBuffer.wrap(pt));
            return copy;
        } else {
            throw new InvalidCiphertextException("Invalid Ciphertext");
        }
    }

    @Override
    public void deleteAlias(DeleteAliasRequest arg0) throws AmazonServiceException, AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public DescribeKeyResult describeKey(DescribeKeyRequest arg0) throws AmazonServiceException, AmazonClientException {
        final String arn = retrieveArn(arg0.getKeyId());

        final KeyMetadata keyMetadata = new KeyMetadata().withArn(arn).withKeyId(arn);
        final DescribeKeyResult describeKeyResult = new DescribeKeyResult().withKeyMetadata(keyMetadata);

        return describeKeyResult;
    }

    @Override
    public void disableKey(DisableKeyRequest arg0) throws AmazonServiceException, AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void disableKeyRotation(DisableKeyRotationRequest arg0) throws AmazonServiceException, AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enableKey(EnableKeyRequest arg0) throws AmazonServiceException, AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enableKeyRotation(EnableKeyRotationRequest arg0) throws AmazonServiceException, AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public EncryptResult encrypt(EncryptRequest req) throws AmazonServiceException, AmazonClientException {
        final byte[] cipherText = new byte[512];
        rnd.nextBytes(cipherText);
        DecryptResult dec = new DecryptResult();
        dec.withKeyId(req.getKeyId()).withPlaintext(req.getPlaintext().asReadOnlyBuffer());
        ByteBuffer ctBuff = ByteBuffer.wrap(cipherText);

        results_.put(new DecryptMapKey(ctBuff, req.getEncryptionContext()), dec);

        String arn = retrieveArn(req.getKeyId());
        return new EncryptResult().withCiphertextBlob(ctBuff).withKeyId(arn);
    }

    @Override
    public GenerateDataKeyResult generateDataKey(GenerateDataKeyRequest req) throws AmazonServiceException,
            AmazonClientException {
        byte[] pt;
        if (req.getKeySpec() != null) {
            if (req.getKeySpec().contains("256")) {
                pt = new byte[32];
            } else if (req.getKeySpec().contains("128")) {
                pt = new byte[16];
            } else {
                throw new UnsupportedOperationException();
            }
        } else {
            pt = new byte[req.getNumberOfBytes()];
        }
        rnd.nextBytes(pt);
        ByteBuffer ptBuff = ByteBuffer.wrap(pt);
        EncryptResult encryptResult = encrypt(new EncryptRequest().withKeyId(req.getKeyId()).withPlaintext(ptBuff)
                .withEncryptionContext(req.getEncryptionContext()));
        String arn = retrieveArn(req.getKeyId());
        return new GenerateDataKeyResult().withKeyId(arn).withCiphertextBlob(encryptResult.getCiphertextBlob())
                .withPlaintext(ptBuff);
    }

    @Override
    public GenerateDataKeyWithoutPlaintextResult generateDataKeyWithoutPlaintext(
            GenerateDataKeyWithoutPlaintextRequest req) throws AmazonServiceException, AmazonClientException {
        GenerateDataKeyResult generateDataKey = generateDataKey(new GenerateDataKeyRequest().withEncryptionContext(
                req.getEncryptionContext()).withNumberOfBytes(req.getNumberOfBytes()));
        String arn = retrieveArn(req.getKeyId());
        return new GenerateDataKeyWithoutPlaintextResult().withCiphertextBlob(generateDataKey.getCiphertextBlob())
                .withKeyId(arn);
    }

    @Override
    public GenerateRandomResult generateRandom() throws AmazonServiceException, AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public GenerateRandomResult generateRandom(GenerateRandomRequest arg0) throws AmazonServiceException,
            AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseMetadata getCachedResponseMetadata(AmazonWebServiceRequest arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GetKeyPolicyResult getKeyPolicy(GetKeyPolicyRequest arg0) throws AmazonServiceException,
            AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public GetKeyRotationStatusResult getKeyRotationStatus(GetKeyRotationStatusRequest arg0)
            throws AmazonServiceException, AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListAliasesResult listAliases() throws AmazonServiceException, AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListAliasesResult listAliases(ListAliasesRequest arg0) throws AmazonServiceException, AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListGrantsResult listGrants(ListGrantsRequest arg0) throws AmazonServiceException, AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListKeyPoliciesResult listKeyPolicies(ListKeyPoliciesRequest arg0) throws AmazonServiceException,
            AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListKeysResult listKeys() throws AmazonServiceException, AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListKeysResult listKeys(ListKeysRequest arg0) throws AmazonServiceException, AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putKeyPolicy(PutKeyPolicyRequest arg0) throws AmazonServiceException, AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ReEncryptResult reEncrypt(ReEncryptRequest arg0) throws AmazonServiceException, AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void retireGrant(RetireGrantRequest arg0) throws AmazonServiceException, AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void revokeGrant(RevokeGrantRequest arg0) throws AmazonServiceException, AmazonClientException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEndpoint(String arg0) {
        // Do nothing
    }

    @Override
    public void setRegion(Region arg0) {
        region_ = arg0;
    }

    @Override
    public void shutdown() {
        // Do nothing
    }

    @Override
    public void updateKeyDescription(UpdateKeyDescriptionRequest arg0) throws AmazonServiceException,
            AmazonClientException {
        throw new UnsupportedOperationException();
    }

    public void deleteKey(final String keyId) {
        final String arn = retrieveArn(keyId);
        activeKeys.remove(arn);
    }

    private String retrieveArn(final String keyId) {
        String arn = keyId;
        if (keyId.contains("arn:") == false) {
            arn = idToArnMap.get(keyId);
        }
        assertExists(arn);
        return arn;
    }

    private void assertExists(String keyId) {
        if (idToArnMap.containsKey(keyId)) {
            keyId = idToArnMap.get(keyId);
        }
        if (keyId == null || !activeKeys.contains(keyId)) {
            throw new NotFoundException("Key doesn't exist");
        }
    }

    private static class DecryptMapKey {
        private final ByteBuffer cipherText;
        private final Map<String, String> ec;

        public DecryptMapKey(DecryptRequest req) {
            cipherText = req.getCiphertextBlob().asReadOnlyBuffer();
            if (req.getEncryptionContext() != null) {
                ec = Collections.unmodifiableMap(new HashMap<String, String>(req.getEncryptionContext()));
            } else {
                ec = Collections.emptyMap();
            }
        }

        public DecryptMapKey(ByteBuffer ctBuff, Map<String, String> ec) {
            cipherText = ctBuff.asReadOnlyBuffer();
            if (ec != null) {
                this.ec = Collections.unmodifiableMap(new HashMap<String, String>(ec));
            } else {
                this.ec = Collections.emptyMap();
            }
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((cipherText == null) ? 0 : cipherText.hashCode());
            result = prime * result + ((ec == null) ? 0 : ec.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            DecryptMapKey other = (DecryptMapKey) obj;
            if (cipherText == null) {
                if (other.cipherText != null)
                    return false;
            } else if (!cipherText.equals(other.cipherText))
                return false;
            if (ec == null) {
                if (other.ec != null)
                    return false;
            } else if (!ec.equals(other.ec))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "DecryptMapKey [cipherText=" + cipherText + ", ec=" + ec + "]";
        }
    }
}
