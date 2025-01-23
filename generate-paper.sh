#!/bin/sh
pdflatex \
  -halt-on-error \
  -output-directory /tmp \
  hadamard.tex \
&& \
  cp /tmp/hadamard.pdf .
