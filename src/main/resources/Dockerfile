FROM phx.ocir.io/sisprod/spectra/java17-ol8-x64:8294a40
WORKDIR /helidon

# Copy the binary built in the 1st stage
COPY target/libs ./libs

CMD ["/bin/sh"]
