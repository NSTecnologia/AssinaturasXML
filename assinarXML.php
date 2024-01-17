<?php

function buscaCertificado($cnpj) {
    $cert = null;
    $lStore = new X509Store("MY", StoreLocation::CurrentUser);
    $lStore->open(OpenFlags::ReadOnly);

    $lcerts = $lStore->certificates;

    foreach ($lcerts as $elementos) {
        if (strpos($elemento->subject, $cnpj) !== false) {
            $cert = $elemento;
            $lStore->close();
            return $cert;
        }
    }

    $lStore->close();
    return $cert;
}

function assinaXML($XMLString, $RefUri, buscaCertificado $X509Cert) {
    $XMLDoc = null;

    try {
        $xnome = "";
        if ($X509Cert !== null) {
            $xnome = $X509Cert->subject;
        }

        $store = new X509Store("MY", StoreLocation::CurrentUser);
        $store->open(OpenFlags::ReadOnly | OpenFlags::OpenExistingOnly);

        $collection = $store->certificates;
        $collection1 = $collection->findBySubjectDistinguishedName($xnome);

        if ($collection1->count() == 0) {
            throw new Exception("Problemas no certificado digital");
        } else {
            $X509Cert = $collection1[0];
            $x = $X509Cert->getKeyAlgorithm()->ToString();

            $doc = new DOMDocument();
            $doc->preserveWhiteSpace = false;
            $doc->loadXML($XMLString);

            $qtdeRefUri = $doc->getElementsByTagName($RefUri)->length;

            if ($qtdeRefUri == 0) {
                throw new Exception("A tag de assinatura " . $RefUri . " inexiste");
            } elseif ($qtdeRefUri > 1) {
                throw new Exception("A tag de assinatura " . $RefUri . " não é única");
            }

            try {
                $signedXml = new SignedXml($doc);
                $signedXml->signingKey = $X509Cert->privateKey;

                $reference = new Reference();
                $Uri = $doc->getElementsByTagName($RefUri)->item(0)->attributes;
                foreach ($Uri as $atributo) {
                    if ($atributo->name == "Id") {
                        $reference->uri = "#" . $atributo->nodeValue;
                    }
                }

                $env = new XmlDsigEnvelopedSignatureTransform();
                $reference->addTransform($env);

                $c14 = new XmlDsigC14NTransform();
                $reference->addTransform($c14);

                $signedXml->addReference($reference);

                $keyInfo = new KeyInfo();
                $keyInfo->addClause(new KeyInfoX509Data($X509Cert));
                $signedXml->keyInfo = $keyInfo;

                $signedXml->computeSignature();
                $xmlDigitalSignature = $signedXml->getXml();

                $doc->documentElement->appendChild($doc->importNode($xmlDigitalSignature, true));
                $XMLDoc = $doc;
                return $XMLDoc->saveXML();
            } catch (Exception $caught) {
                throw new Exception("Erro: Ao assinar o documento - " . $caught->getMessage());
            }
        }
    } catch (Exception $caught) {
        throw new Exception("Erro: Problema ao acessar o certificado digital" . $caught->getMessage());
    }
}
?>
