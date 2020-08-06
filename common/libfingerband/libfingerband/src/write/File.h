#ifndef UNTITLED1_FILEWRITER_H
#define UNTITLED1_FILEWRITER_H

class File {
public:
    virtual ~File() {};
    virtual bool open(const char *file) { return false; };
    virtual bool close() { return false; };
};

#endif //UNTITLED1_FILEWRITER_H
