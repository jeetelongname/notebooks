(ns transducer)

(def xform (map inc))

((xform ((filter even?) +)) 4)
