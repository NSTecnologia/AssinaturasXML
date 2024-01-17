const fs = require('fs');
const forge = require('node-forge');
const SignedXml = require('xml-crypto').SignedXml;

function buscaCertificado(cnpj) {
    let cert = null;
    const certFile = 'path/to/certificate.pem';
    
    const certData = fs.readFileSync(certFile, 'utf8');
    const pki = forge.pki;
    const certObject = pki.certificateFromPem(certData);

    if (certObject.subject.getField('CN').value.indexOf(cnpj) !== -1) {
        cert = certObject;
    }

    return cert;
}

function assinaXML(XMLString, RefUri, X509Cert) {
    let XMLDoc = null;

    try {
        const xnome = X509Cert ? X509Cert.subject.getField('CN').value : '';
        
        const privateKeyPem = fs.readFileSync('path/to/private-key.pem', 'utf8');
        const privateKey = pki.privateKeyFromPem(privateKeyPem);

        const signedXml = new SignedXml();
        signedXml.signingKey = privateKey;

        const reference = {
            uri: '#' + RefUri
        };

        signedXml.addReference(reference);

        const env = "http://www.w3.org/2000/09/xmldsig#enveloped-signature";
        signedXml.addReference(env);

        const c14n = "http://www.w3.org/2001/10/xml-exc-c14n#";
        signedXml.addReference(c14n);

        const keyInfo = new SignedXml.KeyInfo();
        keyInfo.addX509Certificate(X509Cert.raw);
        signedXml.keyInfo = keyInfo;

        signedXml.computeSignature();
        const xmlDigitalSignature = signedXml.getSignatureXml();

        const doc = new DOMParser().parseFromString(XMLString, 'application/xml');
        doc.documentElement.appendChild(doc.importNode(xmlDigitalSignature, true));
        XMLDoc = new XMLSerializer().serializeToString(doc);

        return XMLDoc;
    } catch (error) {
        throw new Error(`Error: ${error.message}`);
    }
}

const cnpj = 'SEU_CNPJ';
const XMLString = 'xml a ser assinado';
const RefUri = 'infNFe';

try {
    const X509Cert = buscaCertificado(cnpj);
    const signedXML = assinaXML(XMLString, RefUri, X509Cert);
    console.log(signedXML);
} catch (error) {
    console.error(error.message);
}
