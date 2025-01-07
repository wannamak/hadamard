#!/bin/sh
pdflatex \
  -halt-on-error \
  -output-directory /tmp \
  about.tex \
&& \
  cp /tmp/about.pdf .
