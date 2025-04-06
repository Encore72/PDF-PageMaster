# PDF PageMaster

## Overview

PDF PageMaster is a desktop application built in Java to process PDF documents.

It has two main functionalities: Numerating documents (Labeling) and paginating them on user selected locations.

It can also add a background to the added text for improved readability.

It works by receiving from the user through drag and drop one or more PDF documents and it processes them, labeling the first page of each document with a user selected text label (for example "DOCUMENT") and that label gets numbered sequentially (DOCUMENT 1, DOCUMENT 2, DOCUMENT 3, DOCUMENT 4...), and it also paginates each page of the submitted documents treating all documents as a single document (for now).

The inspiration to build this app comes from by background as a Lawyer where we tipically have to to submit lots of documents to the court. Sometimes there is lots of documents and in trial you want to be able to quickly refer the judge to a page or a document. This app simplifies that task saving a lot of time.

There is paginating services online, but no useful labeling services so I wanted to build this which has also helped me learn as a personal project.

---


## Future Updates

This is the first version of the application, serving as a functional proof of concept. Future updates will focus on improving UI and expanding user customization options, including:

- Allowing the user to select to add page numbering or document numbering independently.
  
- Choosing between a single merged output or separate numbered PDFs.
  
- Customizing text and background colors.
  
- User will be able to select where to save processed files.
  
- User will be able to input files through "select files" and not only drag and drop.
  
Additional enhancements, such as improved error handling (for example if you try to input a non pdf file), are also planned.

---
