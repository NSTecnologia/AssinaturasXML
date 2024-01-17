# AssinaturasXML

### Realizando assintatura de XML:

Para realizar uma assintatura de XML, você poderá utilizar a função **assinarXML** da dll. Veja abaixo sobre os parâmetros necessários, e um exemplo de chamada do método.

#### Parâmetros:

Parametros    | Tipo de Dado | Descrição
:------------:|:------------:|:-----------
conteudo      | String       | O documente XML a ser assinado.
refURI        | String       | Nodo do XML que deve assinar. Ex.: <ul> <li>**infNFe** - NFe</li> <li>**infNFe** - NFCe</li> <li>**infCTe** - CTe</li> <li>**infMDFe** - MDFe</li> <li>**infBPe** - BPe</li> </ul> .
cnpjEmitente  | String   	 | CNPJ do Emitente da nota para que seja encontrado o certificado.


#### Exemplo de chamada:

Após ter todos os parâmetros listados acima, você deverá fazer a chamada da função. Veja os códigos em Java e PHP dos exemplos abaixo:


Java:

  public class Main {

    public static void main(String[] args) {
        String cnpj = "seu cnpj";
        String XMLString = "xml a ser assinado";
        String RefUri = "infNFe";

        X509Certificate cert = XMLSigner.buscaCertificado(cnpj);

        if (cert != null) {
    
            PrivateKey privateKey = null; // Substitua esta linha com a lógica para obter a chave privada

            String signedXML = XMLSigner.assinaXML(XMLString, RefUri, cert, privateKey);

            if (signedXML != null) {
                System.out.println(signedXML);
            } else {
                System.out.println("Erro ao assinar o XML");
            }
        } else {
            System.out.println("Certificado não encontrado");
        }
    }
}

PHP:

<?php

        $cnpj = "seu cnpj";
        $XMLString = "xml a ser assinado";
        $RefUri = "infNFe";

        try {

            $X509Cert = buscaCertificado($cnpj);

            if ($X509Cert !== null) {

                $signedXML = assinaXML($XMLString, $RefUri, $X509Cert);
                 echo $signedXML;
            } else {
                 echo "Certificado não encontrado";
            }
            } catch (Exception $e) {
                 echo "Erro: " . $e->getMessage();
            }
?>

A função **assinarXML** fará a assinatura do XML e retornará o mesmo assinado em modo de String.