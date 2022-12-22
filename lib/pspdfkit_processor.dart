///
///  Copyright © 2018-2022 PSPDFKit GmbH. All rights reserved.
///
///  THIS SOURCE CODE AND ANY ACCOMPANYING DOCUMENTATION ARE PROTECTED BY INTERNATIONAL COPYRIGHT LAW
///  AND MAY NOT BE RESOLD OR REDISTRIBUTED. USAGE IS BOUND TO THE PSPDFKIT LICENSE AGREEMENT.
///  UNAUTHORIZED REPRODUCTION OR DISTRIBUTION IS SUBJECT TO CIVIL AND CRIMINAL PENALTIES.
///  This notice may not be removed from this file.
///

part of pspdfkit;

const convertorDocumentTitle = 'documentTitle';
const androidConvertorBaseUrl = 'baseUrl';
const iosConvertorPageNumber = 'numberOfPages';
const androidConvertorIsJavascriptEnabled = 'enableJavaScript';

/// This class is used to generate PDF documents from HTML,templates, patterns and images.
class PspdfkitProcessor {
  final MethodChannel _channel;
  static PspdfkitProcessor? _instance;

  PspdfkitProcessor(this._channel);

  /// Returns the singleton instance of [PspdfkitProcessor].
  static PspdfkitProcessor get instance {
    _instance ??= PspdfkitProcessor(const MethodChannel('com.pspdfkit.global'));
    return _instance!;
  }

  /// Generate PDF from Images, Templater, and Patterns.
  ///[pages]: [NewPage]s to be added to the PDF.
  ///[outputPath]: The path to the output file.
  /// Returns the path to the generated PDF path or null if the input is invalid or if the PDF generation fails.
  Future<String?> generatePdf(List<NewPage> pages, String outputPath) async {
    try {
      return await _channel.invokeMethod(
        'generatePDF',
        <String, dynamic>{
          'pages': pages
              .map((page) => page.toMap())
              .toList(), // Converting [NewPage] to map
          'outputFilePath': outputPath,
        },
      );
    } on PlatformException catch (e) {
      if (kDebugMode) {
        print(e);
      }
      return null;
    }
  }

  /// Generates a PDF from HTML string.
  ///
  /// [html]: The HTML string to be converted to PDF.
  /// [outPutFile]: The path to the output file.
  /// Returns the path to the generated PDF file or null if the input is invalid or if the PDF generation fails.
  Future<String?> generatePdfFromHtmlString(String html, String outPutFile,
      [dynamic options]) async {
    if (outPutFile.isEmpty || html.isEmpty) {
      return null;
    }
    try {
      return await _channel
          .invokeMethod('generatePdfFromHtmlString', <String, dynamic>{
        'html': html,
        'outputPath': outPutFile,
        'options': options ?? {'documentTitle': 'Document'}
      });
    } on PlatformException catch (e) {
      if (kDebugMode) {
        print(e);
      }
      return null;
    }
  }

  Future<dynamic> mergePdfs(List<String> pdfPaths, String destPath) async {
    try {
      dynamic file = await _channel.invokeMethod('mergePdfs', <String, dynamic>{
        'listOfPdf': pdfPaths,
        'destPath': destPath,
      });
      return file;
    } on PlatformException catch (e) {
      if (kDebugMode) {
        print(e);
      }
      return null;
    }
  }

  Future<dynamic> getPages(String pdfPath) async {
    try {
      dynamic pageList =
          await _channel.invokeListMethod("getPages", <String, dynamic>{
        'docPath': pdfPath,
      });
      return pageList;
    } on PlatformException catch (e) {
      if (kDebugMode) {
        print(e);
      }
      return null;
    }
  }

  Future<dynamic> splitPdfs(String pdfPath, List<int> selectedIndex,
      List<String> filePathsToStore) async {
    try {
      dynamic pageList =
          await _channel.invokeListMethod('splitPdf', <String, dynamic>{
        'documentPath': pdfPath,
        'selectedIndex': selectedIndex,
        'filePathsToStore': filePathsToStore
      });
      return pageList;
    } on PlatformException catch (e) {
      if (kDebugMode) {
        print(e);
      }
      return null;
    }
  }

  Future<dynamic> getThumbnail(String pdfPath, String thumbnailPath) async {
    try {
      dynamic thumbnail =
          await _channel.invokeMethod('getThumbnail', <String, dynamic>{
        'documentPath': pdfPath,
        'thumbnailPath': thumbnailPath,
      });
      return thumbnail;
    } on PlatformException catch (e) {
      if (kDebugMode) {
        print(e);
      }
      return "";
    }
  }

  /// Generates a PDF from HTML URI.
  /// [htmlUri]: The URI to the HTML file to be converted to PDF. The URI can be for a local file or a remote file.
  /// [outPutFile]: The path to the output file.
  /// Returns the path to the generated PDF file or null if the input is invalid or if the PDF generation fails.
  Future<String?> generatePdfFromHtmlUri(Uri htmlUri, String outPutFile,
      [dynamic options]) async {
    if (outPutFile.isEmpty || htmlUri.toString().isEmpty) {
      return null;
    }
    try {
      return await _channel
          .invokeMethod('generatePdfFromHtmlUri', <String, dynamic>{
        'htmlUri': htmlUri.toString(),
        'outputPath': outPutFile,
        'options': options ?? {'documentTitle': 'Document'}
      });
    } on PlatformException catch (e) {
      if (kDebugMode) {
        print(e);
      }
      return null;
    }
  }
}
